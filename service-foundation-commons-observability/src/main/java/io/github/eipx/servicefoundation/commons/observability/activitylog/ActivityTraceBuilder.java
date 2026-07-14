package io.github.eipx.servicefoundation.commons.observability.activitylog;

public abstract class ActivityTraceBuilder {

    public static final String UNKNOWN = "unknown";
    protected String hostname;
    protected String microservice;
    protected String processId = "0";
    protected String threadId;
    protected String name;

    public ActivityTraceBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ActivityTraceBuilder withMicroservice(String microservice) {
        this.microservice = microservice;
        return this;
    }

    public ActivityTraceBuilder withHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public ActivityTraceBuilder withProcessId(String processId) {
        this.processId = processId;
        return this;
    }

    public ActivityTraceBuilder withThreadId(String threadId) {
        this.threadId = threadId;
        return this;
    }

    public abstract ActivityTrace build();

}
