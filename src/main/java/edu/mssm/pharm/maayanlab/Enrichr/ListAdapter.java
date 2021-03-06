/**
 * JsonSerializer object that helps serialize List
 * 
 * @author		Edward Y. Chen
 * @since		02/25/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ListAdapter implements JsonSerializer<List> {

	@Override
	public JsonElement serialize(List list, Type type, JsonSerializationContext jsc) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("list_id", Shortener.encode(list.getListid()));	// Don't show int count, show encoded version instead
		jsonObject.addProperty("description", list.getDescription());
		jsonObject.addProperty("passkey", list.getPasskey());
		jsonObject.add("created", jsc.serialize(list.getCreated()));
		return jsonObject;
	}

}
