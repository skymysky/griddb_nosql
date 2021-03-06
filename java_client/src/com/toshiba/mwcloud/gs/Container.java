/*
   Copyright (c) 2017 TOSHIBA Digital Solutions Corporation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.toshiba.mwcloud.gs;

import java.io.Closeable;
import java.net.URL;
import java.sql.Blob;

import javax.sql.rowset.serial.SerialBlob;

/**
 * Provides the functions of managing the components of GridDB,
 * each consisting of a set of Rows of a single type.
 *
 * <p>It provides various management functions treating a Row object
 * as a unit of input/output. A Row object and a Row in GridDB are
 * mapped to each other, based on the correspondence between the
 * specified type of a Row object and the schema defined in GridDB.
 * </p>
 *
 * <p>Each Column composing a schema in GridDB has a correspondence
 * relation with a field and methods defined in a Row object. The
 * number of Columns is from 1 to 1024 per Container. The
 * correspondence relation with each column is determined based on
 * the public, protected and default access fields of the specified
 * type or the getter and setter methods, excluding fields and methods
 * specified as {@link TransientRowField} and transient fields.	A
 * default constructor with a public, protected or default access
 * modifier must be prepared to generate a Row object dynamically.
 * Internal classes must be static. </p>
 *
 * <p>The getter is a method with no parameters which has a name
 * beginning with "is" or "get" if it return a Boolean value, or a
 * name with beginning with "get" if it returns any other type value.
 * The setter is a method with only one parameter specifying a setting
 * value which has a name beginning with "set." Unless specified by
 * {@link RowField}, the column names used in GridDB correspond with
 * the character strings obtained by removing prefixes, such as "get,"
 * from the names of field, getter or setter methods. If either (not both) of a
 * getter or a setter is only defined, it is ignored. If a field with
 * the same name and both of a getter and a setter are defined, the
 * getter and the setter are used. If there is a difference in case
 * between a getter and a setter, the getter is given priority. If a
 * Column has a Row key, {@link RowKey} is set on the corresponding
 * field or methods. </p>
 *
 * <p>Multiple column names that are different only in upper-
 * and lowercase letters cannot be defined in a table.
 * Further the allowed characters, the length of column names and
 * the number of columns are limited. See the GridDB Technical
 * Reference for the details. In the operations specifying column
 * names, ASCII uppercase and lowercase characters are identified as
 * same unless otherwise noted. Use {@link RowField} to specify
 * column names that cannot be defined as fields or methods (such as
 * the names in which the first character is a number and Java
 * reserved words).</p>
 *
 * <p>The correspondence between the type of a Column and the type of
 * each value in a Row object is as follows:</p>
 * <table><thead><td>
 * Column type
 * </td>
 * <table>
 * <thead><tr><th>Column type</th><th>Type of each value in a Row object</th></tr></thead>
 * <tbody>
 * <tr><td>STRING</td><td>{@link String}</td></tr>
 * <tr><td>BOOL</td><td>{@link Boolean} or {@code boolean}</td></tr>
 * <tr><td>BYTE</td><td>{@link Byte} or {@code byte}</td></tr>
 * <tr><td>SHORT</td><td>{@link Short} or {@code short}</td></tr>
 * <tr><td>INTEGER</td><td>{@link Integer} or {@code int}</td></tr>
 * <tr><td>LONG</td><td>{@link Long} or {@code long}</td></tr>
 * <tr><td>FLOAT</td><td>{@link Float} or {@code float}</td></tr>
 * <tr><td>DOUBLE</td><td>{@link Double} or {@code double}</td></tr>
 * <tr><td>TIMESTAMP</td><td>{@link java.util.Date}</td></tr>
 * <tr><td>BLOB</td><td>Class implementing {@link Blob}</td></tr>
 * <tr><td>STRING array</td><td>{@code String[]}</td></tr>
 * <tr><td>BOOL array</td><td>{@code boolean[]}</td></tr>
 * <tr><td>BYTE array</td><td>{@code byte[]}</td></tr>
 * <tr><td>SHORT array</td><td>{@code short[]}</td></tr>
 * <tr><td>INTEGER array</td><td>{@code int[]}</td></tr>
 * <tr><td>LONG array</td><td>{@code long[]}</td></tr>
 * <tr><td>FLOAT array</td><td>{@code float[]}</td></tr>
 * <tr><td>DOUBLE array</td><td>{@code double[]}</td></tr>
 * <tr><td>TIMESTAMP array</td><td>{@code java.util.Date[]}</td></tr>
 * </tbody>
 * </table>
 *
 * <p>There are the restrictions on the display range and size of the
 * field value. See the GridDB Technical Reference and the appendix
 * "Range of values" for the details.
 * Values contrary to the restriction cannot be stored in a Container.</p>
 *
 * <p>Restrictions such as the datatypes permitted as a Row key, existence of
 * columns corresponding to the Row key, and permissibility of Row operations
 * differ depending on the definition of the sub-interfaces of this Container.</p>
 *
 * <p>NULL in GridDB rows can be retained unless the NOT NULL constraint is set.
 * When the field of the row object or the getter / setter method can input / output
 * the value of the reference type, NULL in GridDB rows can be input and output as
 * {@code null}. Otherwise, NULL is mapped to an empty value in the row object. </p>
 *
 * <p>The NOT NULL constraint on the row object type can be explicitly specified with
 * {@link NotNull} and {@link Nullable}. If a NOT NULL constraint is not specified for
 * any of the specifications, the column other than the row key is assumed to be without
 * the NOT NULL constraint. The row key is implicitly set to the NOT NULL constraint
 * and cannot be specified to exclude this constraint. Also, it is not possible to specify
 * conflicting NOT NULL constraints between the same object and between getter and setter
 * methods. For example, if {@link NotNull} and {@link Nullable} are specified
 * simultaneously for the row object type, it is assumed that a conflicting NOT NULL
 * constraint was specified. The priority order by designation target on the presence
 * or absence of the NOT NULL constraint is as follows. </p>
 * <ol>
 * <li>Field of row object or getter/setter method</li>
 * <li>Raw object </li>
 * <li>Enclosing of row object (Example: An interface that surrounds the class of the
 * row object as an inner class) or an enclosing that is recursively obtained. Among
 * enclosing types that are determined in recurrence, there are constraint specifications,
 * and the type found first is given priority。</li>
 * <li>Package to which the type of row object belongs to</li>
 * </ol>
 *
 * <p> An empty value is a type of field value that may be used as initial value of various
 * operations such as creation of {@link Row}. Values are defined for each column as follows.</p>
 * <table>
 * <thead><tr><th>Column</th><th>empty value</th></tr></thead>
 * <tbody>
 * <tr><td>STRING</td><td>{@code ""}(String with length 0)</td></tr>
 * <tr><td>BOOL</td><td>False ({@code false})</td></tr>
 * <tr><td>NUMERAL</td><td>{@code 0}</td></tr>
 * <tr><td>TIMESTAMP</td><td>{@code 1970-01-01T00:00:00Z}</td></tr>
 * <tr><td>GEOMETRY</td><td>{@code POINT(EMPTY)}</td></tr>
 * <tr><td>BLOB</td><td>BLOB data of length 0</td></tr>
 * <tr><td>ARRAY</td><td>Array with 0 element</td></tr>
 * </tbody>
 * </table>
 *
 * <p>During transaction processing, the auto commit mode is enabled by default.
 * In the auto commit mode, change operations are confirmed sequentially
 * and cannot be deleted explicitly.
 * In the manual commit mode, if an error in the cluster node is detected
 * by an operation via this object and {@link GSException} is sent out,
 * all update operations before committing are deleted.
 * The transaction separation level supports only READ COMMITTED.
 * The lock particle size differs depending on the type of Container.</p>
 *
 * <p>After generation or transaction of this {@link Container} ends, a new
 * transaction is started at the point the Row is first updated, added or deleted,
 * and the lock for updating purposes is acquired.
 * In the auto commit mode, when the Row operation which started the transaction ends,
 * the transaction is automatically committed and ended.
 * In the manual commit mode, a transaction will not be ended until
 * the validity period for controlling the transaction explicitly is reached.
 * {@link #commit()} is used to commit a transaction while {@link #abort()} is
 * used to abort a transaction.
 * Even if this {@link Container} or the {@link GridStore} of the generation source
 * is closed, the transaction will be aborted and ended.
 * In addition, the time an operation is carried out to start a transaction
 * is adopted as the start point, and the validity period is reached only
 * when the period defined in GridDB has passed. When you try to continue
 * with the Row operations and transaction commitment without aborting
 * after the validity period is reached, {@link GSException} will be sent out.</p>
 *
 * <p>In some cases, operations on the same container may have to wait until
 * the processing on the cluster node is started in response to an operation
 * request to a certain container. Operations here include changes of definitions
 * such as container schema and index, container information reference, row operation,
 * etc. When manipulating containers through {@link GridStore} instances with
 * a consistency level of {@code IMMEDIATE}, in principle, they are made to wait
 * in the middle of other manipulations with the {@code IMMEDIATE} setting for
 * the same container. In principle, processing is not performed based on the state
 * in the middle of other operation processing on the container. For exceptional
 * items, see the explanation for each individual operation. </p>
 *
 * @param <K> the type of a Row key. If no Row key is used, specify Void.
 * @param <R> the type of a Row object used for mapping
 */
public interface Container<K, R> extends Closeable {

	/**
	 * Newly creates or updates a Row, based on the specified Row object
	 * and also the Row key specified as needed.
	 *
	 * <p>If a Column exists which corresponds to the specified Row key,
	 * it determines whether to newly create or update a Row, based on the
	 * Row key and the state of the Container. If there is no corresponding
	 * Row in the Container, it determines to newly create a Row; otherwise,
	 * it updates a relevant Row. If a Row key is specified besides a Row
	 * object, the specified Row key is used in preference to the Row key
	 * in the Row object. </p>
	 *
	 * <p>If no Column exists which corresponds to the specified Row key,
	 * it always creates a new Row. In such a case, specify {@code null}
	 * as key. </p>
	 *
	 * <p>Restrictions are applied depending on the type of Container and
	 * its settings. See the descriptions of subinterfaces for detailed
	 * restrictions. </p>
	 *
	 * <p>In the manual commit mode, the target Row is locked. </p>
	 *
	 * @param key A target Row key
	 * @param row A Row object representing the content of a Row to be newly created
	 * or updated.
	 *
	 * @return {@code TRUE} if a Row exists which corresponds to the
	 * specified Row key.
	 *
	 * @throws GSException if a Row key is specified although no Column
	 * exists which corresponds to the key.
	 * @throws GSException if its operation is contrary to the restrictions
	 * specific to a particular Container.
	 * @throws GSException if a timeout occurs during this operation or
	 * the transaction, this Container is deleted, its schema is changed or
	 * a connection failure occurs; or if called after the connection is
	 * closed; or if an unsupported value is set in the key or the Row object.
	 * @throws ClassCastException if the specified key or Row object does
	 * not completely match the type(s) used in mapping operation.
	 * @throws NullPointerException if {@code null} is specified as {@code row};
	 * For objects in row objects corresponding to row fields, if there is a NOT NULL
	 * constrain but {@code null} is set, it will in include elements of {@code null}
	 * when array type is selected.
	 */
	public boolean put(K key, R row) throws GSException;

	/**
	 * Newly creates or updates a Row, based on the specified Row object only.
	 *
	 * <p>It behaves in the same way as {@link #put(Object, Object)}, except
	 * that it uses a Row key in the specified Row object. </p>
	 *
	 * @see #put(Object, Object)
	 */
	public boolean put(R row) throws GSException;

	/**
	 * Based on the specified Row object group, an arbitrary number of Rows
	 * will be consolidated to create a new group or updated.
	 *
	 * <p>For each Row in the specified Row object group, a new creation or
	 * update operation is carried out just like the case when {@link #put(Object)}
	 * is invoked in accordance with the take-out sequence from the iterator.</p>
	 *
	 * <p>If multiple Rows having the same Row key exist in the specified Row object
	 * group,
	 * the contents of the rear-most Row having the same Row key will be reflected
	 * using the take-out sequence from the iterator of the Row object group
	 * as a reference.</p>
	 *
	 * <p>Depending on the Container type and setting, the same restrictions
	 * as {@link Container#put(Object)} are established for the contents of Rows
	 * that can be operated.
	 * Refer to definition of the sub-interface for the specific restrictions.</p>
	 *
	 * <p>In the manual commit mode, the target Rows are locked.</p>
	 *
	 * <p>In the auto commit mode, if an exclusion occurs in the midst of processing
	 * a Container and its Rows, only the results for some of the Rows
	 * in the Container
	 * may remain reflected.</p>
	 *
	 * @return Always {@code false} in the current version
	 *
	 * @throws GSException When an operation violating the restrictions unique
	 * to a specific Container type is carried out
	 * @throws GSException if a timeout occurs during this operation or
	 * the transaction, this Container is deleted, its schema is changed or
	 * a connection failure occurs; or if called after the connection is
	 * closed; or if an unsupported value is set in the key or the Row object.
	 * @throws ClassCastException if the specified Row objects does not match
	 * the value types of Row objects used
	 * in mapping operation, respectively.
	 * @throws NullPointerException if NULL is specified as {@code rowCollection} or
	 * its element; As with {@link #put(Object, Object)}, if {@code null} is
	 * included in a specific part of the row object.
	 *
	 * @see #put(Object)
	 */
	public boolean put(java.util.Collection<R> rowCollection) throws GSException;

	/**
	 * Returns the content of a Row corresponding to the specified Row key.
	 *
	 * <p>It behaves in the same way as {@link #get(Object, boolean)}
	 * called without requesting a lock for update. </p>
	 *
	 * @see #get(Object, boolean)
	 */
	public R get(K key) throws GSException;

	/**
	 * Returns the content of a Row corresponding to the specified
	 * Row key according to the specified option.
	 *
	 * <p>It can be used only if a Column exists which corresponds to
	 * the specified Row key.</p>
	 *
	 * <p>If it requests a lock for update in the manual commit mode,
	 * it will hold the lock until a relevant transaction completes or
	 * a timeout occurs. The update or deletion operation by any other
	 * transaction on the locked Row will wait until the transaction
	 * completes or a timeout occurs. The lock will be held even if the
	 * target Row is deleted.</p>
	 *
	 * <p>In the autocommit mode, it cannot request a lock for update.</p>
	 *
	 * @param forUpdate indicates whether it requests a lock for update.
	 *
	 * @return A target Row object. {@code null} if no target Row exists.
	 *
	 * @throws GSException if no Column exists which corresponds to the
	 * specified Row key.
	 * @throws GSException if it requests a lock for update in the
	 * autocommit mode.
	 * @throws GSException if a timeout occurs during this operation or
	 * the transaction, this Container is deleted, its schema is changed
	 * or a connection failure occurs; or if called after the connection
	 * is closed; or if an unsupported value is specified as key.
	 * @throws ClassCastException if the specified Row key does not match
	 * the type of a Row key used in mapping operation.
	 * @throws NullPointerException if {@code null} is specified as {@code key}.
	 */
	public R get(K key, boolean forUpdate) throws GSException;

	/**
	 * Deletes a Row corresponding to the specified Row key.
	 *
	 * <p>It can be used only if a Column exists which corresponds to
	 * the specified Row key. If no corresponding Row exists, nothing
	 * is changed. </p>
	 *
	 * <p>Restrictions are applied depending on the type of Container
	 * and its settings. See the descriptions of subinterfaces for
	 * detailed restrictions. </p>
	 *
	 * <p>In the manual commit mode, the target Row is locked.</p>
	 *
	 * @return TRUE if a corresponding Row exists.
	 *
	 * @throws GSException if no Column exists which corresponds to the
	 * specified Row key.
	 * @throws GSException if its operation is contrary to the restrictions
	 * specific to a particular Container.
	 * @throws GSException if a timeout occurs during this operation or the
	 * transaction, this Container is deleted, its schema is changed or a
	 * connection failure occurs; or if called after the connection is closed;
	 * or if an unsupported value is specified as key.
	 * @throws ClassCastException if the specified Row key does not match the
	 * type of a Row key used in mapping operation.
	 * @throws NullPointerException if {@code null} is specified.
	 */
	public boolean remove(K key) throws GSException;

	/**
	 * Creates a query object to execute the specified TQL statement.
	 *
	 * <p>It cannot be used for a query whose output format does not match the
	 * types of Rows in this Container, such as a query containing an aggregation
	 * operation in its selection expression. For such a query,
	 * {@link #query(String, Class)} can be used instead. </p>
	 *
	 * <p>When obtaining a set of Rows using {@link Query#fetch(boolean)},
	 * the option of locking for update can be enabled only for the queries that
	 * will not select Rows which do not exist in this Container. For example, it
	 * cannot be enabled for a query containing an interpolation operation. </p>
	 *
	 * <p> In the current version, due to an error in the TQL statement,
	 * {@link GSException} and by specifying {@code null} with an argument
	 * that cannot specify {@code null}, {@link NullPointerException} will
	 * not be dispatched. If there is an error in the argument, an exception
	 * will be thrown when the resulting query is fetched. </p>
	 *
	 * @param tql TQL statement. {@code null} cannot be specified
	 *
	 * @throws GSException not sent in the current version
	 *
	 * @see #query(String, Class)
	 */
	public Query<R> query(String tql) throws GSException;

	/**
	 * Creates a query object to execute the specified TQL statement and return
	 * the specified type of result.
	 *
	 * <p>It is used for a query whose output format does not match the types of
	 * Rows in this Container, such as an aggregation operation. The following
	 * types and {@code null} are only available as {@code rowType}.</p>
	 * <dl>
	 * <dt>Row type of Container </dt>
	 * <dd>Indicates receiving Row data of the type matching the Rows in this
	 * Container, as in {@link #query(String)}.</dd>
	 * <dt>{@link AggregationResult}</dt>
	 * <dd>Indicates receiving the result of executing an aggregation operation. </dd>
	 * <dt>{@link QueryAnalysisEntry}</dt>
	 * <dd>Indicates receiving the result of executing an EXPLAIN or EXPLAIN
	 * ANALYZE statement.</dd>
	 * <dt>{@code null}</dt>
	 * <dd>Indicates receiving a proper type of result, depending on the operation. </dd>
	 * </dl>
	 * <p>No other value can be specified. </p>
	 *
	 * <p>When obtaining a set of Rows using {@link Query#fetch(boolean)}, the
	 * option of locking for update can be enabled only for the queries that
	 * will not select Rows which do not exist in this Container. For example,
	 * it cannot be enabled for a query containing an interpolation operation.</p>
	 *
	 * <p>In the current version, due to an error in the TQL statement,
	 * {@link GSException} and by specifying {@code null} with an argument
	 * that cannot specify {@code null}, {@link NullPointerException} will
	 * not be dispatched. If there is an error in the argument, an exception
	 * will be thrown when the resulting query is fetched. </p>
	 *
	 * @param tql TQL statement. {@code null} cannot be specified
	 * @param rowType the expected row object type or {@code null}
	 *
	 * @throws GSException if an unsupported type is specified as {@code rowType}.
	 */
	public <S> Query<S> query(
			String tql, Class<S> rowType) throws GSException;

	/**
	 * Creates a Blob to store a large size of binary data for a {@link Container}.
	 *
	 * <p>The created Blob can be used as a Row field. First, set binary data
	 * in the Blob using Blob.setBinaryStream(long) etc. and then store it
	 * in {@link Container} using {@link #put(Object)} etc. </p>
	 *
	 * <p>At least the following methods can be called on the {@link Blob}
	 * obtained by this method.</p>
	 * <ul>
	 * <li>{@link Blob#length()}</li>
	 * <li>{@link Blob#setBinaryStream(long)}</li>
	 * <li>{@link Blob#setBytes(long, byte[])}</li>
	 * <li>{@link Blob#setBytes(long, byte[], int, int)}</li>
	 * <li>{@link Blob#free()}</li>
	 * </ul>
	 *
	 * <p>You do not have to use the {@link Blob} created by this method as
	 * BLOB to be set on a Row object. You can set an instance of
	 * other class implementing {@link Blob}, such as {@link SerialBlob}.
	 * The created {@link Blob} does not have any validity period. </p>
	 *
	 * <p>In the current version, since the entire Row is cached in memory,
	 * it might be impossible to store larger data than the maximum VM
	 * memory size. </p>
	 *
	 * <p>{@link GSException} will not be thrown in the current version. </p>
	 */
	public Blob createBlob() throws GSException;

	/**
	 * Commits the result of the current transaction and start a new
	 * transaction in the manual commit mode.
	 *
	 * @throws GSException if called not in the autocommit mode
	 * @throws GSException if a timeout occurs during this operation
	 * or the transaction, this Container is deleted or a connection
	 * failure occurs; or if called after the connection is closed.
	 */
	public void commit() throws GSException;

	/**
	 * Rolls back the result of the current transaction and starts a new
	 * transaction in the manual commit mode.
	 *
	 * @throws GSException if called not in the autocommit mode.
	 * @throws GSException if a timeout occurs during this operation or
	 * the transaction, this Container is deleted or a connection failure
	 * occurs; or if called after the connection is closed.
	 */
	public void abort() throws GSException;

	/**
	 * Change the setting of the commit mode.
	 *
	 * <p>In the auto commit mode, the transaction state cannot be controlled
	 * directly and change operations are committed sequentially.
	 * If the auto commit mode is disabled, i.e. in the manual commit mode,
	 * as long as the transaction has not timed out or {@link #commit()} has been
	 * invoked directly, the same transaction will continue to be used
	 * in this Container and change operations will not be committed. </p>
	 *
	 * <p>When the autocommit mode is switched from On to Off, uncommitted
	 * updates are committed implicitly. Unless the commit mode is changed,
	 * the state of the transaction will not be changed. These behaviors are
	 * the same as those of {@link java.sql.Connection#setAutoCommit(boolean)}.</p>
	 *
	 * @throws GSException if a timeout occurs during this operation or the
	 * transaction, this Container is deleted or a connection failure occurs,
	 * when a commit is requested after a mode change; or if called after the
	 * connection is closed.
	 */
	public void setAutoCommit(boolean enabled) throws GSException;

	/**
	 * Creates an unnamed index with default type for the column with the specified name.
	 *
	 * <p>Behaves the same as calling {@link #createIndex(IndexInfo)} with {@link IndexInfo}
	 * with only the column name set. </p>
	 *
	 * @throws GSException If the specified column name does not conform to the rule of
	 * {@link #createIndex(IndexInfo)}
	 * @throws GSException if a timeout occurs during this operation, this Container
	 * is deleted, its schema is changed or a connection failure occurs; or if called
	 * after the connection is closed.
	 * @throws GSException if indexing is not supported on the specified Column.
	 * @throws NullPointerException if a {@code null} parameter is specified.
	 */
	public void createIndex(String columnName) throws GSException;

	/**
	 * Creates an unnamed index with the specified type for the column with the specified name.
	 *
	 * <p>Behaves the same as calling {@link #createIndex(IndexInfo)} with
	 * {@link IndexInfo} with only the column name and type set. </p>
	 *
	 * @throws GSException If the specified column name and type does not conform to the rule of
	 * {@link #createIndex(IndexInfo)}
	 * @throws GSException if a timeout occurs during this operation, this Container
	 * is deleted, its schema is changed or a connection failure occurs; or if called
	 * after the connection is closed.
	 * @throws GSException if the specified type of index is not supported on the
	 * specified Column type.
	 * @throws NullPointerException if a {@code null} parameter(s) is specified.
	 */
	public void createIndex(
			String columnName, IndexType type) throws GSException;

	/**
	 * Create an index according to the contents set in {@link IndexInfo}.
	 *
	 * <p>For the column of the index to be created, at least one of the column
	 * name and column number must be set, and the actual container must be set
	 * in the corresponding container. If both column name and column number are
	 * set, corresponding columns must match each other. </p>
	 *
	 * <p>If the index type is not set or {@link IndexType#DEFAULT} is set,
	 * the default index type is selected according to the criteria described below. </p>
	 *
	 * <p>If an index name is set, a new index is created only if
	 * there is no index with the same name or the different name
	 * only in upper- or lowercase letters in the target
	 * container. See the GridDB Technical Reference for the
	 * details. In defining an index name, there are limits on
	 * the allowed characters and the length. In the operations of
	 * index, the names are not case-sensitive unless otherwise
	 * noted.</p>
	 *
	 * <p>If a name index gets duplicated, you must specify the
	 * same setting {@link IndexInfo} that satisfies the
	 * conditions described below, in which case no new index will
	 * be created. On the other hand, you can not specify an
	 * existing {@link IndexInfo} that has the same name as an
	 * index with a different name or an unnamed index. </p>
	 *
	 * <p>If an index name is not set, it is assumed that creation
	 * of an unnamed index was requested. If an identical index
	 * already exists (excluding name), it must be an unnamed
	 * index, which in this case no new index will be
	 * created. </p>
	 *
	 * <p>In the current version, an index created through {@link Container}
	 * is considered to be the same set of indexes
	 * except for index names if the following conditions are
	 * satisfied.</p>
	 * <ul>
	 * <li>The columns to be indexed must match. Differences in column specification methods,
	 * such as column names and column numbers are ignored.</li>
	 * <li>The columns to be indexed must match. Differences in the specification method of
	 * index type such as existence of default designation are ignored.</li>
	 * </ul>
	 *
	 * <p> In the current version, for the {@link Container} instance generated based on
	 * {@link GridStoreFactory#getInstance ()}, the following index type is selected as default
	 * based on the type of container, the type of corresponding column, etc.</p>
	 * <table>
	 * <thead>
	 * <tr><th>Column type</th><th>collection</th><th>time series</th></tr>
	 * </thead>
	 * <tbody>
	 * <tr><td>STRING</td>
	 * <td>{@link IndexType#TREE}</td>
	 * <td>{@link IndexType#TREE}</td></tr>
	 * <tr><td>BOOL</td>
	 * <td>{@link IndexType#TREE}</td>
	 * <td>{@link IndexType#TREE}</td></tr>
	 * <tr><td>NUMERAL</td>
	 * <td>{@link IndexType#TREE}</td>
	 * <td>{@link IndexType#TREE}</td></tr>
	 * <tr><td>TIMESTAMP</td>
	 * <td>{@link IndexType#TREE}</td>
	 * <td>{@link IndexType#TREE} Note:restriction applies</td></tr>
	 * <tr><td>GEOMETRY</td>
	 * <td>{@link IndexType#SPATIAL}</td>
	* <td>(-)</td></tr>
	 * <tr><td>BLOB</td>
	* <td>(-)</td>
	* <td>(-)</td></tr>
	 * <tr><td>ARRAY</td>
	 * <td>(-)</td>
	 * <td>(-)</td></tr>
	 * </tbody>
	 * </table>
	 * <p> An Index cannot be set for Time Series Row Keys (TIMESTAMP type).</p>
	 * <p>If this {@link Container} instance holds an uncommitted transaction,
	 * commit before create. Container to be processed. If there are other transactions
	 * being executed at the same time, wait for them to finish before creating.
	 * If an index already exists and no new index is created, it is undefined whether
	 * to wait by another transaction. In this case, it is undefined whether uncommitted
	 * transactions held by this {@link Container} instance are always committed or not. </p>
	 *
	 * <p>In the current version, in the case of satisfying the
	 * conditions such as the size of the container, during
	 * creating an index, the reference of the container
	 * information, a part of the index operations, the trigger
	 * operations, and the row operations (including the update of
	 * rows) may be performed. Under the definition of {@link Container},
	 * other operations may be waited. For the
	 * operations during the index creation, the container
	 * information doesn't include the index information to be
	 * created.</p>
	 *
	 * @throws GSException When the column or index name to be created does not conform to the above rule
	 * @throws GSException If this process's timeout, deletion of this container or schema change,
	 * connection failure occurs, or when called after closing
	 * @throws GSException When an unsupported index type is specified in the specified column
	 * @throws NullPointerException when {@code null} is specified as argument
	 *
	 */
	public void createIndex(IndexInfo info) throws GSException;

	/**
	 * Delete only the default type index from the column with the specified name.
	 *
	 * <p>Behaves the same as calling {@link #dropIndex(IndexInfo)} with
	 * {@link IndexInfo} with column name and default type set. </p>
	 *
	 * @throws GSException When the specified column name does not conform
	 * to the rule of {@link #dropIndex(IndexInfo)}
	 * @throws GSException if a timeout occurs during this operation, this
	 * Container is deleted, its schema is changed or a connection failure
	 * occurs; or if called after the connection is closed.
	 * @throws NullPointerException if a {@code null} parameter is specified.
	 */
	public void dropIndex(String columnName) throws GSException;

	/**
	 * Deletes only the index of the specified type from the column with the specified name.
	 *
	 * <p>Behaves the same as calling {@link #dropIndex(IndexInfo)} with {@link IndexInfo}
	 * with column name and type set. </p>
	 *
	 * @throws GSException when the specified column name does not conform to the rule of
	 * {@link #dropIndex(IndexInfo)}
	 * @throws GSException if a timeout occurs during this operation, this Container
	 * is deleted, its schema is changed or a connection failure occurs; or if called
	 * after the connection is closed.
	 * @throws NullPointerException if {@code null} is specified as more than
	 * one parameter.
	 */
	public void dropIndex(
			String columnName, IndexType type) throws GSException;

	/**
	 * Delete all indexes that match the content set in {@link IndexInfo}.
	 *
	 * <p>The setting information of {@link IndexInfo} are used as a condition
	 * to narrow down the index to be deleted. Filtering conditions are classified
	 * into three categories: column, index type, and index name. Setting each
	 * of them is optional. If none of them are set, all created indexes are deleted. </p>
	 *
	 * <p>If a column name or column number is set, it must exist in the corresponding
	 * container. If both column name and column number are set, corresponding columns
	 * must match each other. If neither the column name nor the column number is set,
	 * the index for any column that satisfies other refinement conditions
	 * (index type, index name) will be deleted. </p>
	 *
	 * <p>When the index type is set, only the index of the specified type will be
	 * deleted. If {@link IndexType#DEFAULT} is set, the default type index is
	 * selected according to the standard of {@link #createIndex(IndexInfo)}.
	 * Columns that do not support indexes and columns that do not support indexes
	 * of the specified type are not eligible for deletion. If the index type is not set,
	 * index that fulfil the conditions (column, index name) will be deleted. </p>
	 *
	 * <p>If an index name is set, only the index with the specified name will be deleted.
	 * The identity of the index name follows the criteria of {@link #createIndex(IndexInfo)}.
	 * If an index name is not set, an index with an arbitrary name and an unnamed index
	 * that fulfils the conditions (column, index type) will be deleted. </p>
	 *
	 * <p>If there is no index to be deleted, the index will not get deleted.</p>
	 *
	 * <p>Transaction handling is similar to {@link #createIndex(IndexInfo)}. Also,
	 * it is undefined whether or not other transactions may be executed while only
	 * a few indexes are deleted when multiple indexes are subject to deletion. </p>
	 *
	 * <p>The immediate state after completion of the index deletion request is similar
	 * to {@link GridStore#dropContainer(String)}. </p>
	 *
	 * @throws GSException if the column or index name to be deleted does not conform to the above rule
	 * @throws GSException if the process is timeout, deletion of this container or schema change,
	 * connection failure occurs, or called after closing
	 * @throws NullPointerException when {@code null} is specified as argument
	 *
	 */
	public void dropIndex(IndexInfo info) throws GSException;

	/**
	 * @deprecated
	 */
	@Deprecated
	public void createEventNotification(URL url) throws GSException;

	/**
	 * @deprecated
	 */
	@Deprecated
	public void dropEventNotification(URL url) throws GSException;

	/**
	 *
	 * Set the trigger.
	 *
	 * <p>If a update operation of a specific type is carried out on this Container,
	 * a notification will be sent to the specified URI.
	 * If a trigger with the same name as the specified trigger exists,
	 * the setting will be overwritten.</p>
	 *
	 * <p>Refer to the definition of {@link TriggerInfo} for the detailed trigger
	 * settings. The details of the trigger name, the trigger type, notification conditions,
	 * notification destination URI and notification contents are as shown below.</p>
	 *
	 * <b>Trigger name</b>
	 * <p>Multiple trigger names which are identified as the same,
	 * including the names only different in ASCII uppercase and
	 * lowercase characters, even if they have different types or
	 * notification conditions, in a container cannot be
	 * defined. And there are the limitations, the allowed characters
	 * and the length, on the trigger names. See the GridDB
	 * Technical Reference for the details. Trigger names are
	 * case-sensitive unless otherwise noted.</p>
	 *
	 * <b>Trigger type</b>
	 * <p>The following trigger types are supported.
	 * <table>
	 * <thead><tr><th>name</th><th>description</th></tr></thead>
	 * <tbody>
	 * <tr>
	 * <td>REST</td>
	 * <td>Trigger to notify the specified URI with a REST notification (HTTP POST method)
	 * when an update operation of the specified type is carried out on a Container. </td>
	 * </tr>
	 * <tr>
	 * <td>Java Message Service(JMS)</td>
	 * <td>Trigger to send a JMS message to the JMS server of the specified URI
	 * when an update operation of the specified type is carried out on a Container.
	 * Apache ActiveMQ is used as the JMS provider.</td>
	 * </tr>
	 * </tbody>
	 * </table>
	 *
	 * <b>Notification condition</b>
	 * <p>Create new Row/ update Row for this Container
	 * ({@link Container#put(Object)},
	 * {@link Container#put(Object, Object)},
	 * {@link Container#put(java.util.Collection)},
	 * {@link GridStore#multiPut(java.util.Map)},
	 * {@link RowSet#update(Object)}),
	 * delete ({@link Container#remove(Object)}, {@link RowSet#remove()})
	 * Perform notification immediately after executing operation command.
	 * If multiple operations are specified as monitoring targets,
	 * perform notification after executing any one of these operations.</p>
	 *
	 * <p>Completion of replication at the point notification is carried out
	 * is not guaranteed.
	 * For notifications corresponding to a create new Row/ update Row or
	 * delete command that has been executed by disabling the auto commit mode,
	 * if the transaction is not committed at the point of the notification,
	 * or if the transaction is aborted after the notification, it may not be
	 * possible to get the data included in the notification at the point
	 * the notification is received.</p>
	 *
	 * <p>For batch operations involving multiple Rows, notification is carried out
	 * for each Row operation.
	 * If there is no response within a specified time even if notification has been
	 * sent to the specified URl, time out is performed and it will not be sent again.
	 * If a failure occurs in a GridDB cluster, in addition to not sending
	 * any notification to support a certain update operation, multiple notifications
	 * may be sent.</p>
	 *
	 * <b>Notification destination URI</b>
	 * <p>
	 * A notification destination URI is described in the following format.</p>
	 * <pre>
	 * (method name)://(host name):(port number)/(path)</pre>
	 * <p>However, if the trigger type is REST, only http can be specified in the method name.</p>
	 *
	 * <b>Notification contents</b>
	 * <p>Provide notification of the updated Container name, update operation name,
	 * and specified column value of the updated Row data.
	 * For the update operation name, use {@code "put"} to create a new Row/ update
	 * Row and {@code "delete"} to delete.</p>
	 *
	 * <p>The notification value shall be the specified column value of the Row data
	 * that is newly created immediately after a new Row is created, or updated
	 * in an update operation, or before deletion in a delete operation.
	 * If the column type is TIMESTAMP, an integer to indicate the time passed
	 * in milliseconds starting from {@code 1970-01-01T00:00:00Z} is set as the value.
	 * If the column type if BLOB, GEOMETRY, or array, a blank character string
	 * will be set as the value.</p>
	 *
	 * <b>Notification method - For REST</b>
	 * <p>JSON character strings such as those shown below are sent with the MIME type application/json.</p>
	 * <pre>
	 * {
	 *   "container" : "(container name)",
	 *   "event" : "(update operation name)",
	 *   "row" : {
	 *     "(column name)" : (column data),
	 *     "(column name)" : (column data),
	 *     ...
	 *   }
	 * }</pre>
	 *
	 * <b>Notification method - For JMS</b>
	 * <p>A javax.jms.TextMessage is sent with the specified destination type
	 * and destination name.</p>
	 *
	 * <p>The container name is set by
	 * {@code javax.jms.Message#setStringProperty("@container", "(container name)")}.
	 * The update operation name is set by
	 * {@code javax.jms.Message#setStringProperty("@event", "(update operation name)")}.</p>
	 *
	 * <p>The column value is set with a {@code javax.jms.Message#setXXXProperty("(column name)", (column data))}
	 * in accordance with the column type.</p>
	 *
	 * <p>When the column layout is changed by a
	 * {@link GridStore#putCollection(String, Class, boolean)},
	 * {@link GridStore#putTimeSeries(String, Class, TimeSeriesProperties, boolean)}, etc.
	 * in relation to a Container with a set trigger, if a column subject
	 * to trigger notification is deleted or if its name is changed,
	 * the corresponding column will be deleted from the trigger notification targets.</p>
	 *
	 * <p>If the server does not respond to a request sent to the notification
	 * destination URI that has been set up when sending a notification
	 * from GridDB, standby processing will occur until the process times out.
	 * This standby process becomes a cause for the delay in serving notification
	 * of an update in the Container as well as some other containers.
	 * Therefore, a trigger having an invalid notification destination URI
	 * is recommended to be deleted by using {@link #dropTrigger(String)}.</p>
	 *
	 * <p>See the GridDB Technical Reference for the maximum number of
	 * triggers that can be set for a single Container and the upper limit of the
	 * values for various trigger settings.</p>
	 *
	 * @param info Trigger information of the setting target
	 *
	 * @throws GSException If the trigger name is {@code null}, blank, or does not follow to other rules
	 * @throws GSException If the update operation subject to monitoring is not specified
	 * @throws GSException If the notification destination URI does not conform to the stipulated syntax
	 * @throws GSException If the JMS is specified by the trigger type, and the JMS destination type is {@code null},
	 * or is blank, or does not conform to the specified format
	 * @throws GSException If the JMS is specified by the trigger type, and the JMS destination name is {@code null},
	 * or is blank
	 * @throws GSException If the JMS is specified by the trigger type, and the JMS destination name is {@code null},
	 * or is blank
	 * @throws NullPointerException If {@code null} is specified in the argument
	 *
	 */
	public void createTrigger(TriggerInfo info) throws GSException;

	/**
	 * Delete a trigger.
	 *
	 * <p>Nothing is changed if the trigger of the specified name does not exist.</p>
	 *
	 * @throws GSException If this process times out, this Container is deleted, a connection failure were to occur,
	 * or if it is invoked after being closed
	 * @throws NullPointerException If {@code null} is specified in the argument
	 */
	public void dropTrigger(String name) throws GSException;

	/**
	 * Writes the results of earlier updates to a non-volatile storage medium,
	 * such as SSD, so as to prevent the data from being lost even if all
	 * cluster nodes stop suddenly.
	 *
	 * <p>It can be used for operations requiring higher reliability than
	 * normal. However, frequent execution of this would cause degradation
	 * in response time. </p>
	 *
	 * <p>Its behaviors vary, such as the scope of cluster nodes concerned,
	 * depending on the configuration of GridDB. </p>
	 *
	 * @throws GSException if a timeout occurs during this operation, this
	 * Container is deleted or a connection failure occurs; or if called
	 * after the connection is closed.
	 */
	public void flush() throws GSException;

	/**
	 * Disconnects with GridDB and releases related resources
	 * as necessary.
	 *
	 * <p>When a transaction is held, uncommitted updates will be
	 * rolled back. </p>
	 *
	 * <p>Even if {@link GSException} is thrown, the connection and local
	 * resources will be released properly. However, the transaction state
	 * might remain in GridDB. If the transaction is already closed, no
	 * release operation is invoked by this method. </p>
	 *
	 * @throws GSException if a connection failure occurs
	 */
	public void close() throws GSException;

	/**
	 * Get the type of this Container.
	 *
	 * <p>In the current version, no inquiry is sent to the GridDB cluster
	 * by this operation as the type is always confirmed at the point
	 * the instance is generated.</p>
	 *
	 * @throws GSException If invoked after being closed
	 */
	public ContainerType getType() throws GSException;

	/**
	 * Create a new Row object based on the column layout of this Container.
	 *
	 * <p>If the Row object type of the Container is {@link Row}, a fixed
	 * default value is set in each field of the {@link Row} to be created,
	 * similar to the case when it is created using
	 * a {@link GridStore#createRow(ContainerInfo)}.
	 * In this case, the operation on the created {@link Row} also does not affect
	 * whether this {@link Container} object is closed or not.</p>
	 *
	 * @throws GSException If an exclusion is sent out when creating a user-defined
	 * Row object
	 * @throws GSException If invoked after being closed
	 */
	public R createRow() throws GSException;

}
