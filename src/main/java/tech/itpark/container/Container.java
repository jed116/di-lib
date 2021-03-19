package tech.itpark.container;

import tech.itpark.exception.AmbiguousConstructorException;
import tech.itpark.exception.DependencyInjectionException;
import tech.itpark.exception.UnmetDependencyException;

import java.lang.reflect.Constructor;
import java.util.*;

public class Container {
    // fqn
    private final Map<String, Object> objectMap = new HashMap<>();
    private final Collection<Definition> definitions = new LinkedList<>();


    private Object primitiveArgumentCasting(String typeName, Object value){
    try {
        if (typeName.trim().toLowerCase().contains("bool")) {
            return ((Boolean) value).booleanValue();
        };
        if (typeName.trim().toLowerCase().contains("int")) {
            return ((Double) value).intValue();
        };
        if (typeName.trim().toLowerCase().contains("long")) {
            return ((Double) value).longValue();
        };
        if (typeName.trim().toLowerCase().contains("double")) {
            return ((Double) value).doubleValue();
        };
        if (typeName.trim().toLowerCase().contains("float")) {
            return ((Double) value).floatValue();
        };
        if (typeName.trim().toLowerCase().contains("string")) {
            return (String) value;
        };
    }catch (Exception e){}
        return null;
    }

    private Optional<Constructor<?>> findConstructor(Definition definition, Map<String, Object> objects,
                                           List<Object> constructorParameters){
        try{
           final Class<?> cls = Class.forName(definition.getName());

            Object[] definitionParameters = definition.getParameters();
            String[] definitionDependencies = definition.getDependencies();

            for (Constructor<?> constructor : cls.getConstructors()) {
// constructors iterating
                int constructorParameterCount = constructor.getParameterCount();
                if (constructorParameterCount == definitionParameters.length + definitionDependencies.length) {
                    Map<String, Object> copyObjects = new HashMap<>(objects);

                    constructorParameters.clear();
                    int indexParam = 0;
                    for (Class<?> constructorParameterType : constructor.getParameterTypes()) {
// constructor parameters iterating
                        String typeName = constructorParameterType.getTypeName();
                        Object objArg = copyObjects.get(typeName);
                        if (objArg != null) {
                            constructorParameters.add(objArg);
                            copyObjects.remove(typeName);
                            continue;
                        }

                        if (definitionParameters.length <= indexParam) {
                            break;
                        }
                        objArg = primitiveArgumentCasting(typeName, definitionParameters[indexParam++]);
                        if (objArg == null) {
                            break;
                        }

                        constructorParameters.add(objArg);
                    }

                    if (constructorParameterCount == constructorParameters.size()) {
                        return Optional.ofNullable(constructor);
                    }
                }
            }
            return Optional.empty();
        } catch (Exception e){
           throw new DependencyInjectionException(e);
        }
    }


    // varargs - переменное количество аргументов
    public void register(Definition... definitions) { // definitions на самом деле массив
        this.definitions.addAll(Arrays.asList(definitions));
    }

    // связывание зависимостей (создаём из определений объекты)
    public void wire() {
        try {
            List<Definition> lostDefinitions = new LinkedList<>(definitions);

            int lostSize = lostDefinitions.size();
            var iterator = lostDefinitions.iterator();
            while (iterator.hasNext()) {
                final var definition = iterator.next();

                if (!objectMap.keySet().containsAll(Arrays.asList(definition.getDependencies()))) {continue;}

                List<Object> foundConstructorParameters = new LinkedList<>();
                Constructor<?> foundConstructor = findConstructor(definition, objectMap, foundConstructorParameters)
                    .orElseThrow(()-> new AmbiguousConstructorException("Сould not find the required constructor for definition " +
                        definition.getName()));

                final Object obj = foundConstructor.newInstance(foundConstructorParameters.toArray());
                objectMap.put(obj.getClass().getName(), obj);

                // interfaces
                for (Class<?> iface : Class.forName(definition.getName()).getInterfaces()) {
                    String ifaceName = iface.getName();
                    if (objectMap.containsKey(ifaceName)) {
                        throw new AmbiguousConstructorException("To many implements for interface " + ifaceName);
                    }
                    objectMap.put(ifaceName, obj);
                }

                iterator.remove();

                if (!iterator.hasNext() && lostSize != lostDefinitions.size()) {
                    lostSize = lostDefinitions.size();
                    iterator = lostDefinitions.iterator();
                }
            }

            if (objectMap.size() == definitions.size()){
                return;
            }

            if (lostDefinitions.size() != 0) {
                StringBuilder builder = new StringBuilder();
                lostDefinitions.forEach(x -> builder.append("\n- " + x.getName()));
                throw new UnmetDependencyException("Unmet dependency for :" + builder.toString() + "\nNot found all dependencies!!!");
            }

        } catch (Exception e) {
            throw new DependencyInjectionException(e);
        }
    }

    public Object getObjectByName(String name){
        return objectMap.get(name);
    }
}
