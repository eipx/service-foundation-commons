package io.github.eipx.servicefoundation.commons.observability.activitylog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static io.github.eipx.servicefoundation.commons.observability.activitylog.ActivityField.Type.*;

/**
 * Definition of an activity trace that can be logged in the activity log.
 */
public class ActivityTrace implements Serializable {

    @ActivityField(column = 1, name = "container_id", description = "Id of the container", type = STRING)
    protected String hostname;
    @ActivityField(column = 2, name = "micro_service", description = "Name of the micro-service", type = STRING)
    protected String microservice;
    @ActivityField(column = 3, name = "process_id", description = "Process ID", type = STRING)
    protected String processId = "0";
    @ActivityField(column = 4, name = "thread_id", description = "Thread ID", type = STRING)
    protected String threadId;
    @ActivityField(column = 5, name = "activity", description = "Activity", type = STRING)
    protected String name;

    private ActivityTrace() {
        hostname = "";
        microservice = "";
        processId = "";
        threadId = "";
        name = "";
    }

    public ActivityTrace(String hostname, String microservice, String processId, String threadId, String name) {
        this();
        this.hostname = hostname;
        this.microservice = microservice;
        this.processId = processId;
        this.threadId = threadId;
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public String getMicroservice() {
        return microservice;
    }

    public String getProcessId() {
        return processId;
    }

    public String getThreadId() {
        return threadId;
    }

    public String getName() {
        return name;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setMicroservice(String microservice) {
        this.microservice = microservice;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(hostname);
        builder.append("|");
        builder.append(microservice);
        builder.append("|");
        builder.append(processId);
        builder.append("|");
        builder.append(threadId);
        builder.append("|");
        builder.append(name);
        buildExtraAttributes().forEach(o -> {
            builder.append("|");
            builder.append(o);
        });
        return builder.toString();
    }

    protected List<String> buildExtraAttributes() {
        return new ArrayList<>();
    }
}
