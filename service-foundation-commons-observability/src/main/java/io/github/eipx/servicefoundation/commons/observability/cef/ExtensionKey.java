package io.github.eipx.servicefoundation.commons.observability.cef;

import org.springframework.lang.Nullable;

import static org.apache.commons.lang3.StringUtils.*;

import static io.github.eipx.servicefoundation.commons.observability.cef.CEFEventSerializer.*;


public enum ExtensionKey {

    CS3("cs3") {
        protected Object getValue(CEFEvent event) {
            return event.getCs3();
        }
    },

    CS3_LABEL("cs3Label") {
        protected Object getValue(CEFEvent event) {
            return event.getCs3Label();
        }
    },

    CS4("cs4") {
        protected Object getValue(CEFEvent event) {
            return event.getCs4();
        }
    },

    CS4_LABEL("cs4Label") {
        protected Object getValue(CEFEvent event) {
            return event.getCs4Label();
        }
    },

    MSG("msg") {
        protected Object getValue(CEFEvent event) {
            return event.getMsg();
        }
    },

    CAT("cat") {
        protected Object getValue(CEFEvent event) {
            return event.getCat();
        }
    },

    DEVICE_PROCESS_NAME("deviceProcessName") {
        protected Object getValue(CEFEvent event) {
            return event.getDeviceProcessName();
        }
    },

    SRC("src") {
        protected Object getValue(CEFEvent event) {
            return event.getSrc();
        }
    },

    DTZ("dtz") {
        protected Object getValue(CEFEvent event) {
            return event.getDtz();
        }
    },

    RT("rt") {
        protected Object getValue(CEFEvent event) {
            return event.getRt();
        }
    },

    CS2("cs2") {
        protected Object getValue(CEFEvent event) {
            return event.getCs2();
        }
    },

    CS2_LABEL("cs2Label") {
        protected Object getValue(CEFEvent event) {
            return event.getCs2Label();
        }
    },

    CS1("cs1") {
        protected Object getValue(CEFEvent event) {
            return event.getCs1();
        }
    },

    CS1_LABEL("cs1Label") {
        protected Object getValue(CEFEvent event) {
            return event.getCs1Label();
        }
    };

    private final String name;

    ExtensionKey(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    @Nullable
    final String formatKeyValuePair(CEFEvent event) {
        Object value = getValue(event);
        if (value != null) {
            if (value instanceof String) {
                value = escape((String) value).trim();
            }
            return name + "=" + value;
        } else {
            return null;
        }
    }

    protected abstract Object getValue(CEFEvent event);

    private String escape(String string) {
        return replaceEach(string, EXTENSION_CHARACTERS_TO_ESCAPE, EXTENSION_ESCAPED_CHARACTERS);
    }
}
