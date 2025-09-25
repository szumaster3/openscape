package core.cache

import com.displee.cache.CacheLibrary
import com.displee.cache.ProgressListener
import com.displee.cache.index.Index
import core.cache.def.impl.ItemDefinition
import core.cache.def.impl.SceneryDefinition
import core.net.event.js5.DataProvider
import core.tools.SystemLogger
import java.nio.ByteBuffer

/**
 * Cache system management, provides methods to access cache files.
 * [Displee cache library](https://github.com/Displee/rs-cache-library/tree/master)
 */
object Cache {
    /**
     * The main cache library used to interact with cache files and indices.
     */
    private lateinit var cacheLibrary: CacheLibrary

    /**
     * The progress listener for monitoring cache loading progress.
     */
    private lateinit var progressListener: ProgressListener

    /**
     * A byte array representing the version table, which holds CRC and revision info for
     * each cache index.
     */
    lateinit var versionTable: ByteArray

    /**
     * Provides data to be sent via the JS5 protocol.
     */
    lateinit var provider: DataProvider

    /**
     * Initializes the cache system with the given cache path.
     * @param cachePath The file path to the cache directory.
     */
    @JvmStatic
    fun init(cachePath: String?) {
        progressListener = SystemLogger.CreateProgressListener()
        cacheLibrary = CacheLibrary(cachePath.toString(), false, progressListener)
        // InterfacePacking(cacheLibrary).packInterfaces()
        versionTable = generateUKeys()
        provider = DataProvider(cacheLibrary)
        parseDefinitions()
    }

    /**
     * Parses item and scenery definitions from the cache.
     * Called during cache initialization.
     */
    private fun parseDefinitions() {
        ItemDefinition.parse()
        SceneryDefinition.parse()
    }

    /**
     * Gets a version table by iterating through all cache indices and collecting their CRC
     * and revision information. This table is used to check the integrity and
     * versioning of cache data.
     *
     * @return A byte array containing CRC and revision info for each cache index.
     */
    private fun generateUKeys(): ByteArray {
        val buffer = ByteBuffer.allocate(cacheLibrary.indices().size * 8)
        for (index in cacheLibrary.indices()) {
            buffer.putInt(index.crc)
            buffer.putInt(index.revision)
        }
        return buffer.array()
    }

    /**
     * Gets an `Index` object for a given [index] of the cache.
     *
     * @param index The cache index to retrieve.
     * @return The corresponding `Index` object.
     */
    fun getIndex(index: CacheIndex): Index = cacheLibrary.index(index.id)

    /**
     * Gets data from the cache for the index, archive, and file.
     *
     * @param index The cache index to search.
     * @param archive The archive ID within the index.
     * @param file The file ID within the archive.
     * @return A byte array of the requested data, or null if not found.
     */
    @JvmStatic
    fun getData(
        index: CacheIndex,
        archive: Int,
        file: Int,
    ): ByteArray? = cacheLibrary.data(index.id, archive, file, null)

    /**
     * Gets data from the cache for the index and archive.
     *
     * @param index The cache index to search.
     * @param archive The archive within the index.
     * @param file The file ID within the archive.
     * @return A byte array of the requested data, or null if not found.
     */
    @JvmStatic
    fun getData(
        index: CacheIndex,
        archive: CacheArchive,
        file: Int,
    ): ByteArray? = cacheLibrary.data(index.id, archive.id, file, null)

    /**
     * Gets data from the cache for the index and archive using a string identifier.
     *
     * @param index The cache index to search.
     * @param archive The name of the archive.
     * @return A byte array of the requested data, or null if not found.
     */
    @JvmStatic
    fun getData(
        index: CacheIndex,
        archive: String,
    ): ByteArray? = cacheLibrary.data(index.id, archive, 0)

    /**
     * Gets data from the cache for the index and archive using a string identifier and decryption keys.
     *
     * @param index The cache index to search.
     * @param archive The name of the archive.
     * @param xtea The decryption keys used to retrieve the archive data.
     * @return A byte array of the requested data, or null if not found.
     */
    @JvmStatic
    fun getData(
        index: CacheIndex,
        archive: String,
        xtea: IntArray,
    ): ByteArray? = cacheLibrary.data(index.id, archive, 0, xtea)

    /**
     * Gets the archive ID for a given archive name in an index.
     *
     * @param index The cache index to search.
     * @param archive The name of the archive.
     * @return The archive ID, or -1 if not found.
     */
    @JvmStatic
    fun getArchiveId(
        index: CacheIndex,
        archive: String,
    ): Int = cacheLibrary.index(index.id).archiveId(archive)

    /**
     * Gets the number of files within a given archive in a specific cache index.
     *
     * @param index The cache index to search.
     * @param archive The archive to check.
     * @return The number of files in the archive, or -1 if the archive is not found.
     */
    @JvmStatic
    fun getArchiveCapacity(
        index: CacheIndex,
        archive: CacheArchive,
    ): Int =
        cacheLibrary
            .index(index.id)
            .archive(archive.id)
            ?.files()
            ?.size ?: -1

    /**
     * Gets the number of files in a specific archive within a cache index.
     *
     * @param index The cache index to search.
     * @param archive The archive ID within the index.
     * @return The number of files in the archive, or -1 if the archive is not found.
     */
    @JvmStatic
    fun getArchiveFileCount(
        index: CacheIndex,
        archive: Int,
    ): Int {
        val archiveObject = cacheLibrary.index(index.id).archive(archive)
        return archiveObject?.files()?.size ?: -1
    }

    /**
     * Gets the total capacity of a given cache index, which is the size of the files in the
     * last archive plus the ID of the last archive multiplied by 256.
     *
     * @param index The cache index to check.
     * @return The total capacity of the index.
     */
    @JvmStatic
    fun getIndexCapacity(index: CacheIndex): Int {
        val lastArchive = (cacheLibrary.index(index.id).archives().last())
        return (lastArchive.files().size) + (lastArchive.id * 256)
    }
}
