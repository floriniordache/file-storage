# Simple REST file storage service written in Spring Boot

This is a simple file storage service written using Spring Boot, Java 8 and maven

## REST API

The server supports the following methods

### Creating & updating files

**PUT /api/v1/files/{fileName}**

This will create or update the {fileName} in the server's storage with the received request payload

Response types:

- 201 CREATED - for newly created files
- 200 OK - for existing files, on content update

**GET /api/v1/files/{fileName}**

Read the contents for file {fileName}. Supports caching via Last-Modified / If-Modified-Since headers

Response types:

- 200 OK - File's contents will be returned in the response body
- 304 NOT_MODIFIED - If request contains a If-Modified-Since header, a 304 not modified may be returned depending on the file's last modified timestamp on the server
- 404 NOT_FOUND - Given file {fileName} was not found in this storage

**DELETE /api/v1/files/{fileName}**

Remove the file {fileName} from the storage

Response types:

- 200 OK - File was successfully deleted from the storage
- 404 NOT_FOUND - Given file {fileName} was not found in this storage

### Enum operations

**GET /api/v1/files/size**

Returns the number of files stored in this file storage server as JSON

Example response:
<pre>
{
    "filesCount": 1645
}
</pre>

**GET /api/v1/files/enum/{regex}?startIndex={start}&pageSize={maxResults}**

Scans the stored files searching matches for the {regex} regular expression
Result will use pagination so that server resources are not exhausted for very large response sizes (i.e. search for everything .* etc). Pagination will be controlled via {start} starting index and returning at most {maxResults} items.
Additionally, {maxResults} is limited to 1000 items at most.

Example response:
<pre>
{
    "startIndex": 0,
    "itemCount": 5,
    "matchingItems": [
    "file1.txt",
    "file2.txt",
    "file3.txt",
    "file4.txt",
    "file5.txt"
    ]
}
</pre>

## Summary details

- Server will store files in the filesystem under the {filestorage.repo.folder} (default="storage") folder.
- File operation APIs will use mostly the file system to create/update/delete the files
- All incoming file PUTs are stored as temporary files (app will auto create a "temp" folder if not already present). Once the file contents have been successfully saved to temp folder, the resulting file will be moved atomically (if possible) at it's final destination
- Storage size is cached in memory
- Enumeration operation does not do direct disk traversal to find matches, but rather uses an internal file-based index.
    - Index is automatically built on app startup via storage traversal
    - All modifying operations (PUT, DELETE) are write-through, in the sense that, for successful operations, the index will also be updated with the newly added (or removed file).
    - Index updates are applied asynchronously (using separate threads for added/deleted file names), hence the enum operation would be slightly inconsistent until the index updates are committed
