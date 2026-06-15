package cz.deefeen.hardcore;

import cz.deefeen.hardcore.locales.LocaleCZ;
import cz.deefeen.hardcore.locales.LocaleEN;

import java.util.Map;

public class LocaleManager {

    private final Map<String, String> messages;

    public LocaleManager(String lang) {
        if ("en".equalsIgnoreCase(lang)) {
            messages = new LocaleEN().getMessages();
        } else {
            messages = new LocaleCZ().getMessages();
        }
    }

    public String get(String key) {
        return messages.getOrDefault(key, "§cMissing locale: " + key);
    }

    public String format(String key, String... replacements) {
        String msg = get(key);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            String val = replacements[i + 1] != null ? replacements[i + 1] : "";
            msg = msg.replace("{" + replacements[i] + "}", val);
        }
        return msg;
    }
}
