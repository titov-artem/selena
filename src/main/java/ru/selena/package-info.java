/**
 * Tis is Selena DB root package
 *
 * Guarantees:
 *  - Atomicity for one node and one key
 *  - NWR consistency
 *  - If you are saving older data, than have been already available in database, than they can become visible from some
 *    nodes, which haven't got newest version yet
 *  - Responsibility for data versioning rely on client. Data comparison never performed, only version comparison performs.
 *    Newer version always rewrite older version
 */
package ru.selena;
