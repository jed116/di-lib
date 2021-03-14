package tech.itpark.container;

public class Definition {
    private String name;
    private String[] dependencies;
    private Object[] parameters;

    public String getName() {
        return name;
    }
    public String[] getDependencies() { return dependencies; }
    public Object[] getParameters() {return parameters;}

    public void setName(String name) {
        this.name = name;
    }
    public void setDependencies(String[] dependencies) {
        this.dependencies = dependencies;
    }
    public void setParameters(Object[] parameters) {this.parameters = parameters;}
}
