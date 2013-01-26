/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.tables.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.odktables.entity.OdkTablesKeyValueStoreEntry;
import org.opendatakit.tables.sync.SyncUtil;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * A helper class to access values in the key value store. The partition must
 * be set in the creation of the object, ensuring that this helper can only
 * access those keys in its partition.
 * @author sudar.sam@gmail.com
 *
 */
public class KeyValueStoreHelper implements KeyValueHelper {
  
  private static final String TAG = KeyValueStoreHelper.class.getName();
  
  /**
   * This is the default aspect that will be used when interacting with the 
   * key value store via this object. If a named aspect is required (note that
   * it cannot share the same name as this field), 
   * {@link AspectKeyValueStoreHelper} must be used.
   */
  public static final String DEFAULT_ASPECT = "default";
  
  /*
   * This is the partition which this helper will be restricted to.
   */
  private final String partition;
  private final KeyValueStore kvs;
  private final DbHelper dbh;
  private final ObjectMapper mapper;
  
  /**
   * @param kvs
   * @param partition
   */
  public KeyValueStoreHelper(KeyValueStore kvs, String partition) {
    this.partition = partition;
    this.kvs = kvs;
    this.dbh = kvs.getDbHelper();
    this.mapper = new ObjectMapper();
    mapper.setVisibilityChecker(mapper.getVisibilityChecker()
        .withFieldVisibility(Visibility.ANY));
  }
  
  /**
   * The partition of the key value store.
   * @return
   */
  public String getPartition() {
    return this.partition;
  }

  @Override
  public Integer getInteger(String key) {
    return getInteger(DEFAULT_ASPECT, key);
  }
  
  private Integer getInteger(String aspect, String key) {
    OdkTablesKeyValueStoreEntry entry = getEntry(aspect, key);
    if (entry == null) {
      return null;
    }
    if (!entry.type.equals(KeyValueStoreEntryType.INTEGER.getLabel())) {
      throw new IllegalArgumentException("requested int entry for " +
          "key: " + key + ", but the corresponding entry in the store was " +
          "not of type: " + KeyValueStoreEntryType.INTEGER.getLabel());     
    }
    return Integer.parseInt(entry.value);    
  }

  @Override
  public ArrayList<Object> getList(String key) {
    return getList(DEFAULT_ASPECT, key);
  }
  
  private ArrayList<Object> getList(String aspect, String key) {
    OdkTablesKeyValueStoreEntry entry = getEntry(aspect, key);
    if (entry == null) {
      return null;
    }
    if (!entry.type.equals(KeyValueStoreEntryType.ARRAYLIST.getLabel())) {
      throw new IllegalArgumentException("requested int entry for " +
          "key: " + key + ", but the corresponding entry in the store was " +
          "not of type: " + KeyValueStoreEntryType.ARRAYLIST.getLabel());     
    }
    ArrayList<Object> result = null;
    try {
      result = mapper.readValue(entry.value, ArrayList.class);
    } catch (JsonParseException e) {
      Log.e(TAG, "problem parsing json list entry from the kvs");
      e.printStackTrace();
    } catch (JsonMappingException e) {
      Log.e(TAG, "problem mapping json list entry from the kvs");
      e.printStackTrace();
    } catch (IOException e) {
      Log.e(TAG, "i/o problem with json for list entry from the kvs");
      e.printStackTrace();
    }
    return result;    
  }

  @Override
  public String getString(String key) {
    return getString(DEFAULT_ASPECT, key);
  }
  
  private String getString(String aspect, String key) {
    OdkTablesKeyValueStoreEntry entry = getEntry(aspect, key);
    if (entry == null) {
      return null;
    }
    if (!entry.type.equals(KeyValueStoreEntryType.TEXT.getLabel())) {
      throw new IllegalArgumentException("requested int entry for " +
          "key: " + key + ", but the corresponding entry in the store was " +
          "not of type: " + KeyValueStoreEntryType.TEXT.getLabel());     
    }
    return entry.value;    
  }

  @Override
  public String getObject(String key) {
    return getObject(DEFAULT_ASPECT, key);
  }
  
  private String getObject(String aspect, String key) {
    OdkTablesKeyValueStoreEntry entry = getEntry(aspect, key);
    if (entry == null) {
      return null;
    }
    if (!entry.type.equals(KeyValueStoreEntryType.OBJECT.getLabel())) {
      throw new IllegalArgumentException("requested int entry for " +
          "key: " + key + ", but the corresponding entry in the store was " +
          "not of type: " + KeyValueStoreEntryType.OBJECT.getLabel());     
    }
    return entry.value;    
  }

  @Override
  public Boolean getBoolean(String key) {
    return getBoolean(DEFAULT_ASPECT, key);
  }
  
  private Boolean getBoolean(String aspect, String key) {
    OdkTablesKeyValueStoreEntry entry = getEntry(aspect, key);
    if (entry == null) {
      return null;
    }
    if (!entry.type.equals(KeyValueStoreEntryType.BOOLEAN.getLabel())) {
      throw new IllegalArgumentException("requested int entry for " +
          "key: " + key + ", but the corresponding entry in the store was " +
          "not of type: " + KeyValueStoreEntryType.BOOLEAN.getLabel());     
    }
    return SyncUtil.intToBool(Integer.parseInt(entry.value));    
  }

  @Override
  public Double getNumeric(String key) {
    return getNumeric(DEFAULT_ASPECT, key);
  }
  
  private Double getNumeric(String aspect, String key) {
    OdkTablesKeyValueStoreEntry entry = getEntry(aspect, key);
    if (entry == null) {
      return null;
    }
    if (!entry.type.equals(KeyValueStoreEntryType.NUMBER.getLabel())) {
      throw new IllegalArgumentException("requested int entry for " +
          "key: " + key + ", but the corresponding entry in the store was " +
          "not of type: " + KeyValueStoreEntryType.NUMBER.getLabel());     
    }
    return Double.parseDouble(entry.value);   
  }

  @Override
  public void setIntegerEntry(String key, Integer value) {
    setIntegerEntry(DEFAULT_ASPECT, key, value);
  }
  
  private void setIntegerEntry(String aspect, String key, Integer value) {
    if (value == null) {
      removeEntry(key);
      return;
    }
    SQLiteDatabase db = dbh.getWritableDatabase();
    kvs.insertOrUpdateKey(db, this.partition, aspect, key, 
        KeyValueStoreEntryType.INTEGER.getLabel(), Integer.toString(value));
    Log.d(TAG, "updated partition: " + partition + ", aspect: " + 
        aspect + ", key: " + key + " to " + value);    
  }

  @Override
  public void setNumericEntry(String key, Double value) {
    setNumericEntry(DEFAULT_ASPECT, key, value);
  }
  
  private void setNumericEntry(String aspect, String key, Double value) {
    if (value == null) {
      removeEntry(key);
      return;
    }
    SQLiteDatabase db = dbh.getWritableDatabase();
    kvs.insertOrUpdateKey(db, this.partition, aspect, key, 
        KeyValueStoreEntryType.NUMBER.getLabel(), Double.toString(value));
    Log.d(TAG, "updated partition: " + partition + ", aspect: " + 
        aspect + ", key: " + key + " to " + value);   
  }

  @Override
  public void setObjectEntry(String key, String jsonOfObject) {
    setObjectEntry(DEFAULT_ASPECT, key, jsonOfObject);
  }
  
  private void setObjectEntry(String aspect, String key, String jsonOfObject) {
    if (jsonOfObject == null) {
      removeEntry(key);
      return;
    }
    SQLiteDatabase db = dbh.getWritableDatabase();
    kvs.insertOrUpdateKey(db, this.partition, aspect, key, 
        KeyValueStoreEntryType.OBJECT.getLabel(), jsonOfObject);
    Log.d(TAG, "updated partition: " + partition + ", aspect: " + 
        aspect + ", key: " + key + " to " + jsonOfObject);   
  }

  @Override
  public void setBooleanEntry(String key, Boolean value) {
    setBooleanEntry(DEFAULT_ASPECT, key, value);
  }
  
  /**
   * Set the boolean entry for this aspect and key.
   * @param aspect
   * @param key
   * @param value
   */
  private void setBooleanEntry(String aspect, String key, Boolean value) {
    if (value == null) {
      removeEntry(key);
      return;
    }
    SQLiteDatabase db = dbh.getWritableDatabase();
    kvs.insertOrUpdateKey(db, this.partition, aspect, key, 
        KeyValueStoreEntryType.BOOLEAN.getLabel(), 
        Integer.toString(SyncUtil.boolToInt(value)));
    Log.d(TAG, "updated partition: " + partition + ", aspect: " + 
        aspect + ", key: " + key + " to " + value);    
  }

  @Override
  public void setStringEntry(String key, String value) {
    setStringEntry(DEFAULT_ASPECT, key, value);
  }
  
  /**
   * Set the given String entry.
   * @param aspect
   * @param key
   * @param value
   */
  private void setStringEntry(String aspect, String key, String value) {
    if (value == null) {
      removeEntry(key);
      return;
    }
    SQLiteDatabase db = dbh.getWritableDatabase();
    kvs.insertOrUpdateKey(db, this.partition, aspect, key, 
        KeyValueStoreEntryType.TEXT.getLabel(), value);
    Log.d(TAG, "updated partition: " + partition + ", aspect: " + 
        aspect + ", key: " + key + " to " + value);    
  }

  @Override
  public void setListEntry(String key, ArrayList<Object> value) {
    setListEntry(DEFAULT_ASPECT, key, value);
  }
  
  /**
   * Set the list entry for the given aspect and key.
   * @param aspect
   * @param key
   * @param value
   */
  private void setListEntry(String aspect, String key, 
      ArrayList<Object> value) {
    if (value == null) {
      removeEntry(key);
      return;
    }
    String entryValue = null;
    try {
      entryValue = mapper.writeValueAsString(value);
    } catch (JsonGenerationException e) {
      Log.e(TAG, "problem parsing json list entry while writing to the kvs");
      e.printStackTrace();
    } catch (JsonMappingException e) {
      Log.e(TAG, "problem mapping json list entry while writing to the kvs");
      e.printStackTrace();
    } catch (IOException e) {
      Log.e(TAG, "i/o exception with json list entry while writing to the" +
            " kvs");
      e.printStackTrace();
    }
    if (entryValue == null) {
      Log.e(TAG, "problem parsing list to json, not updating key");
      return;
    }
    SQLiteDatabase db = dbh.getWritableDatabase();
    kvs.insertOrUpdateKey(db, this.partition, aspect, key, 
        KeyValueStoreEntryType.ARRAYLIST.getLabel(), entryValue);
    Log.d(TAG, "updated partition: " + partition + ", aspect: " + 
        aspect + ", key: " + key + " to " + value);   
  }

  @Override
  public int removeEntry(String key) {
    return removeEntry(DEFAULT_ASPECT, key);
  }
  
  /**
   * Remove the entries for the given aspect and key.
   * @param aspect
   * @param key
   * @return
   */
  private int removeEntry(String aspect, String key) {
    SQLiteDatabase db = dbh.getWritableDatabase();
    return kvs.deleteKey(db, this.partition, aspect, key);
  }

  @Override
  public OdkTablesKeyValueStoreEntry getEntry(String key) {
    return getEntry(DEFAULT_ASPECT, key);
  }
  
  /**
   * Return the entry for the given aspect and key, using the partition field.
   * <p>
   * Return null if the given entry doesn't exist. Logging is done if there is
   * more than one key matching the specifications, as this as an error. The
   * first entry in the list is still returned, however.
   * @param aspect
   * @param key
   * @return
   */
  private OdkTablesKeyValueStoreEntry getEntry(String aspect, String key) {
    SQLiteDatabase db = dbh.getReadableDatabase();
    List<String> keyList = new ArrayList<String>();
    keyList.add(key);
    List<OdkTablesKeyValueStoreEntry> entries = 
        kvs.getEntriesForKeys(db, this.partition, aspect, keyList);
    // Do some sanity checking. There should only ever be one entry per key.
    if (entries.size() > 1) {
      Log.e(TAG, "request for key: " + key + " in KVS " + 
          kvs.getDbBackingName() +
          " for table: " + kvs.getTableId() + " returned " + entries.size() + 
          "entries. It should return at most 1, as it is a key in a set.");
    }
    if (entries.size() == 0) {
      return null;
    } else {
      return entries.get(0);
    }   
  }
  
  /**
   * Much like the outer KeyValueStoreHelper class, except that this also 
   * specifies an aspect. All the methods apply to the partition of the 
   * enclosing class and the aspect of this class.
   * @author sudar.sam@gmail.com
   *
   */
  public class AspectHelper implements KeyValueHelper {
    
    private final String aspect;
    
    public AspectHelper(String aspect) {
      this.aspect = aspect;
    }

    @Override
    public Integer getInteger(String key) {
      return KeyValueStoreHelper.this.getInteger(aspect, key);
    }

    @Override
    public ArrayList<Object> getList(String key) {
      return KeyValueStoreHelper.this.getList(aspect, key);
    }

    @Override
    public String getString(String key) {
      return KeyValueStoreHelper.this.getString(aspect, key);
    }

    @Override
    public String getObject(String key) {
      return KeyValueStoreHelper.this.getObject(aspect, key);
    }

    @Override
    public Boolean getBoolean(String key) {
      return KeyValueStoreHelper.this.getBoolean(aspect, key);
    }

    @Override
    public Double getNumeric(String key) {
      return KeyValueStoreHelper.this.getNumeric(aspect, key);
    }

    @Override
    public void setIntegerEntry(String key, Integer value) {
      KeyValueStoreHelper.this.setIntegerEntry(aspect, key, value);
    }

    @Override
    public void setNumericEntry(String key, Double value) {
      KeyValueStoreHelper.this.setNumericEntry(aspect, key, value);
    }

    @Override
    public void setObjectEntry(String key, String jsonOfObject) {
      KeyValueStoreHelper.this.setObjectEntry(aspect, key, jsonOfObject);
    }

    @Override
    public void setBooleanEntry(String key, Boolean value) {
      KeyValueStoreHelper.this.setBooleanEntry(aspect, key, value);
    }

    @Override
    public void setStringEntry(String key, String value) {
      KeyValueStoreHelper.this.setStringEntry(aspect, key, value);
    }

    @Override
    public void setListEntry(String key, ArrayList<Object> value) {
      KeyValueStoreHelper.this.setListEntry(aspect, key, value);
    }

    @Override
    public int removeEntry(String key) {
      return KeyValueStoreHelper.this.removeEntry(aspect, key);
    }

    @Override
    public OdkTablesKeyValueStoreEntry getEntry(String key) {
      return KeyValueStoreHelper.this.getEntry(aspect, key);
    }
    
  }

 
}
