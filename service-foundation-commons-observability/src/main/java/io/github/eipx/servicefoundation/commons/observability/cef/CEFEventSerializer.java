package io.github.eipx.servicefoundation.commons.observability.cef;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;

import static java.util.Arrays.*;
import static java.util.Objects.*;

import static org.apache.commons.lang3.StringUtils.*;

public class CEFEventSerializer {

    private static final String[] PREFIX_FIELD_CHARACTERS_TO_ESCAPE = {"|", "\\", "\r", "\n"};
    private static final String[] PREFIX_FIELD_ESCAPED_CHARACTERS = {"\\|", "\\\\", "\\r", "\\n"};

    protected static final String[] EXTENSION_CHARACTERS_TO_ESCAPE = {"\\", "=", "\r", "\n"};
    protected static final String[] EXTENSION_ESCAPED_CHARACTERS = {"\\\\", "\\=", "\\r", "\\n"};

    private static final char PREFIX_FIELDS_SEPARATOR = '|';
    private static final char EXTENSION_KEY_VALUE_PAIRS_SEPARATOR = ' ';

    private static final String[] PREFIX_FIELD_CHARACTERS_TO_UNESCAPE = reverse(PREFIX_FIELD_ESCAPED_CHARACTERS);
    private static final String[] PREFIX_FIELD_UNESCAPED_CHARACTERS = reverse(PREFIX_FIELD_CHARACTERS_TO_ESCAPE);

    private static final String[] EXTENSION_CHARACTERS_TO_UNESCAPE = reverse(EXTENSION_ESCAPED_CHARACTERS);
    private static final String[] EXTENSION_UNESCAPED_CHARACTERS = reverse(EXTENSION_CHARACTERS_TO_ESCAPE);

    private final List<ExtensionKey> extensionKeys;

    public CEFEventSerializer() {
        this(asList(ExtensionKey.values()));
    }

    public CEFEventSerializer(List<ExtensionKey> extensionKeys) {
        this.extensionKeys = ImmutableList.copyOf(requireNonNull(extensionKeys, "extensionKeys"));
    }

    public String serialize(CEFEvent event) {
        return "CEF:" + formatPrefixFields(event) + formatExtension(event);
    }

    public CEFEvent deserialize(String serializedEvent) throws InvalidCefEventException {
        String[] splitLines = serializedEvent.split("(?<!\\\\)\\|",8);
        if (splitLines.length < 7) {
            throw new InvalidCefEventException("Invalid CEF event does not have the mandatory 7 fields");
        }
        int cef;
        try {
            cef = Integer.parseInt(splitLines[0].replace("CEF:", ""));
        } catch (NumberFormatException e) {
            throw new InvalidCefEventException("Invalid CEF could not get CEF version");
        }
        String deviceVendor = unescapePrefixField(splitLines[1]);
        String deviceProduct = unescapePrefixField(splitLines[2]);
        String deviceVersion = unescapePrefixField(splitLines[3]);
        String signatureId = unescapePrefixField(splitLines[4]);
        String name = unescapePrefixField(splitLines[5]);
        CEFEvent.Severity severity = CEFEvent.Severity.fromValue(splitLines[6])
                                                      .orElseThrow(() -> new IllegalArgumentException("Unsupported severity '" + splitLines[6] + "'"));
        String cs1 = null;
        String cs1Label = null;
        String cs2 = null;
        String cs2Label = null;
        String cs3 = null;
        String cs3Label = null;
        String cs4 = null;
        String cs4Label = null;
        String msg = null;
        String cat = null;
        String deviceProcessName = null;
        String src = null;
        String dtz = null;
        Long rt = null;
        if (splitLines.length > 7) {
            Map<String, String> keyValuePairs = parseExtension(splitLines[7]);
            cs1 = unescapeExtension(keyValuePairs.get(ExtensionKey.CS1.getName()));
            cs1Label = unescapeExtension(keyValuePairs.get(ExtensionKey.CS1_LABEL.getName()));
            cs2 = unescapeExtension(keyValuePairs.get(ExtensionKey.CS2.getName()));
            cs2Label = unescapeExtension(keyValuePairs.get(ExtensionKey.CS2_LABEL.getName()));
            cs3 = unescapeExtension(keyValuePairs.get(ExtensionKey.CS3.getName()));
            cs3Label = unescapeExtension(keyValuePairs.get(ExtensionKey.CS3_LABEL.getName()));
            cs4 = unescapeExtension(keyValuePairs.get(ExtensionKey.CS4.getName()));
            cs4Label = unescapeExtension(keyValuePairs.get(ExtensionKey.CS4_LABEL.getName()));
            msg = unescapeExtension(keyValuePairs.get(ExtensionKey.MSG.getName()));
            cat = unescapeExtension(keyValuePairs.get(ExtensionKey.CAT.getName()));
            deviceProcessName = unescapeExtension(keyValuePairs.get(ExtensionKey.DEVICE_PROCESS_NAME.getName()));
            src = unescapeExtension(keyValuePairs.get(ExtensionKey.SRC.getName()));
            String formattedRt = trimToNull(keyValuePairs.get(ExtensionKey.RT.getName()));
            if (formattedRt != null) {
                rt = Long.valueOf(formattedRt);
            }
            dtz = unescapeExtension(keyValuePairs.get(ExtensionKey.DTZ.getName()));
        }

        return new CEFEvent(cef,
                            deviceVendor,
                            deviceProduct,
                            deviceVersion,
                            signatureId,
                            name,
                            severity,
                            cs1,
                            cs1Label,
                            cs2,
                            cs2Label,
                            cs3,
                            cs3Label,
                            cs4,
                            cs4Label,
                            msg,
                            cat,
                            deviceProcessName,
                            src,
                            dtz,
                            rt);
    }

    private static <T> T[] reverse(T[] array) {
        T[] newArray = ObjectArrays.newArray(array, array.length);
        System.arraycopy(array, 0, newArray, 0, array.length);
        ArrayUtils.reverse(newArray);
        return newArray;
    }

    private String formatPrefixFields(CEFEvent event) {
        return new StringBuilder()
                .append(event.getVersion())
                .append(PREFIX_FIELDS_SEPARATOR)
                .append(escapePrefixField(event.getDeviceVendor()))
                .append(PREFIX_FIELDS_SEPARATOR)
                .append(escapePrefixField(event.getDeviceProduct()))
                .append(PREFIX_FIELDS_SEPARATOR)
                .append(escapePrefixField(event.getDeviceVersion()))
                .append(PREFIX_FIELDS_SEPARATOR)
                .append(escapePrefixField(event.getSignatureId()))
                .append(PREFIX_FIELDS_SEPARATOR)
                .append(escapePrefixField(event.getName()))
                .append(PREFIX_FIELDS_SEPARATOR)
                .append(escapePrefixField(event.getSeverity().getValue()))
                .append(PREFIX_FIELDS_SEPARATOR)
                .toString();
    }

    private String escapePrefixField(String string) {
        return replaceEach(string, PREFIX_FIELD_CHARACTERS_TO_ESCAPE, PREFIX_FIELD_ESCAPED_CHARACTERS);
    }

    private String formatExtension(CEFEvent event) {
        StringBuilder extension = new StringBuilder();
        for (ExtensionKey extensionKey : extensionKeys) {
            String keyValuePair = extensionKey.formatKeyValuePair(event);
            if (keyValuePair != null) {
                if (extension.length() > 0) {
                    extension.append(EXTENSION_KEY_VALUE_PAIRS_SEPARATOR);
                }
                extension.append(keyValuePair);
            }
        }
        return extension.toString();
    }

    private Map<String, String> parseExtension(String extension) {
        //(?<!...) is called a "zero-width lookbehind".
        // In English, you're splitting on all = characters that are NOT preceded by a double slash, without actually matching the double slash
        // We do not want to split on \= because they can be part of a message.
        String[] keyValuePairs = extension.split("(?<!\\\\)=");
        Map<String, String> keyValuePairsMap = new HashMap<>();
        String keyValue = null;
        for (int i = 0; i < keyValuePairs.length - 2; i++) {
            String[] valueArray = keyValuePairs[i + 1].split(" ");
            String valueString = String.join(" ", Arrays.copyOfRange(valueArray, 0, valueArray.length - 1));
            if (keyValue == null) {
                keyValue = keyValuePairs[i].trim();
            }
            keyValuePairsMap.put(keyValue, valueString);
            keyValue = valueArray[valueArray.length - 1];
        }
        keyValuePairsMap.put(keyValue, keyValuePairs[keyValuePairs.length - 1].trim());
        return keyValuePairsMap;
    }

    private String unescapePrefixField(String string) {
        return replaceEach(string, PREFIX_FIELD_CHARACTERS_TO_UNESCAPE, PREFIX_FIELD_UNESCAPED_CHARACTERS);
    }

    private String unescapeExtension(String string) {
        return replaceEach(string, EXTENSION_CHARACTERS_TO_UNESCAPE, EXTENSION_UNESCAPED_CHARACTERS);
    }

}
