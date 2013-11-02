package com.mixshare.rapid_evolution.ui.util;

import java.util.ResourceBundle;

import com.mixshare.rapid_evolution.RE3Properties;
import com.mixshare.rapid_evolution.util.StringUtil;

public class Translations {

	static private ResourceBundle res = ResourceBundle.getBundle("translations");

	/**
	 * Returns the translated string.  The first String passed should be the key of the translated text.
	 * Additional Strings should be added in pairs, the first is the sub-text key and the replacement text
	 * for dynamic variables.
	 */
	static public String get(String... keys) {
		try {
			String key = keys[0];
			String result = res.getString(key);
			if (result != null) {
				if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower"))
					result = result.toLowerCase();
				else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper"))
					result = result.toUpperCase();								
			}
			if (keys.length > 1) {
				// Alternate subkey and replacement text...
				int l = 1;
				while (l < keys.length) {
					String subkey = keys[l++];
					if (l < keys.length) {
						String replacement = keys[l++];
						String subkeyCased = Translations.getPreferredCase(subkey);
						result = StringUtil.replace(result, subkeyCased, replacement);
					} else {
						// Unexpected, but don't bail.
					}
				}
			}
			return result;
		} catch (java.util.MissingResourceException mre) {
			return "** missing translation, id=" + keys[0] + " **";
		}
	}

	static public boolean has(String key) {
		try {
			return res.getString(key) != null;
		} catch (java.util.MissingResourceException mre) {
			return false;
		}
	}
	
	static public String getPreferredCase(String text) {
		String result = text;
		if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("lower"))
			result = result.toLowerCase();
		else if (RE3Properties.getProperty("preferred_text_case").equalsIgnoreCase("upper"))
			result = result.toUpperCase();
		return result;		
	}
	
}
