/**
 * <p>
 * This package is designed to serialize and deserialize objects, with four design goals in mind:
 * <ul>
 *     <li>support for circular references</li>
 *     <li>minimal boilerplate and manual configuration</li>
 *     <li>the ability to serialize 3rd party data structures</li>
 *     <li>less data usage than Java's internal serialization API</li>
 * </ul>
 * </p>
 *
 * <p>
 * The algorithm will be passed an object, and made to convert the entire connected object graph into a byte sequence.
 * The algorithm used is as follows:
 * <ol>
 *     <li>
 *         The graph of objects will be converted into a set of objects using BFS/DFS. For each object encountered, a service
 *         will be invoked to assign it an object serializer service, and each serializer service will be assigned a unique ID.
 *         The service will be invoked to determine which further objects should be added to the set.
 *     </li>
 *     <li>
 *         The serializer service of each object will be invoked to produce a sequence of bytes and byte placeholders,
 *         which may include multi-byte primitives and references to other objects.
 *     </li>
 *     <li>
 *         Objects with the same serializer service will be clustered together.
 *     </li>
 *     <li>
 *         With knowledge of required metadata in mind, a map will be constructed between all objects and the byte
 *         sequence that is their reference address.
 *     </li>
 *     <li>
 *         This map will be provided to all byte placeholders, giving reference placeholders an opportunity to memorize
 *         their byte sequence.
 *     </li>
 *     <li>
 *         The byte placeholder sequences will be converted into real byte sequences.
 *     </li>
 *     <li>
 *         These byte sequences, plus metadata, will be combined into the final serialized byte sequence.
 *     </li>
 * </ol>
 * </p>
 *
 * <p>
 * Once the objects are serialized, they must be deserialized into the original object structure.
 * The algorithm used for this is as follows:
 * <ol>
 *     <li>
 *         Metadata will be used to extract the byte sequence for each object, as well as the object serializer service
 *         ID.
 *     </li>
 *     <li>
 *         A service will be invoked to assign each byte sequence an object deserialization service.
 *     </li>
 *     <li>
 *         Each deserialization service will be invoked to produce an object-former.
 *     </li>
 *     <li>
 *         Object-formers will be made to produce their objects, but they need not be in final forms. To be specific,
 *         references do not yet need to be connected.
 *     </li>
 *     <li>
 *         A map will be made between reference addresses and the objects produced by object-formers.
 *     </li>
 *     <li>
 *         This map will be provided to all object formers, giving them a chance to connect references within their objects.
 *     </li>
 *     <li>
 *         Metadata will be used to find the root object, and that object will be returned.
 *     </li>
 * </ol>
 * </p>
 */
package com.phoenixkahlo.nodenet.smartserial;