# Overview

File uploaders are used to transfer files from the local environment (browser) to some remote location (i.e. an S3 or GS bucket). The mechanism consists of a general `IFileUploader` interface with concrete implementations generally extending `FileUploader`.

# Design details

## `IFileUploader`

This interface defines the behaviour expected of an uploader. This is essentially trivialised to a single `send(File, IFileUploaderListener)` method that takes a `File` instance to upload an an `IFileUploaderListener` listener to listen to receive lifecycle events.

## `FileUploader`

This is an abstract support class that supports concrete implementations. When one invokes `send(...)` an instance of `FileSender` is created via `createSender(...)`. This is an abstract method that sub-classes of `FileUploader` are expected to supply. The class has, itself, two abstract methods:

1. `createXMLHttpRequest(...)` which creates and configures an `XMLHttpRequest`.
2. `createFormData(...)` which creates and configures a suitable `FormData` which is then sent by the `XMLHttpRequest` returned in (1) , or returns `null` in which case the raw data is sent.

These will need to be implemented by sub-classes.

The general process of uploading a file flows as follows (as embodied in `FileUploader`):

1. The `send(...)` method is invoked with a `File` and listener.
2. An instance of `FileSender` is created via `createSender(...)`. This method is expected to configure `FileSender` with a unique file reference that is ultimately passed back to the caller on success and is used to reference the file subsequent to upload (and is thus implementation specific). *Note that this is not needed until the upload is started (so can be supplied during start by overriding `start()` - `GCPSignedUrlFileUploader` does this).*
3. During construction of `FileSender` the file is chunked into parts. Chunks (instances of `FileChunk`) are created (by slicing the file) of that size (with the exception of the last chunk, naturally) of size (as possible) determined by `chunkSize` (passed through the constructor of `FileUploader`). Not all upload endpoints support chunking in which case a `chunkSize` of zero creates a single chunk in all cases.
4. The `FileSender`'s `start()` method is onvoked. Here the chunks are themselves started subject to `maxConcurrent` (also passed through the constructor of `FileUploader`). If this is 0 then all chunks are started at once, otherwise a maximum of `maxConcurrent` are started. As chunks complete the `process()` method is called which will start up more chunks as needed to ensure all chunks are eventually started (and completed).
5. Once all chunks are completed (successfully) then the upload is deemed complete and `onSuccess(String)` is invoked on the listener (passed is the file reference specified in (2)).
6. If any of the chunks fails (by error, timeout or being aborted) all remaining chunks are aborted and `onFailure(FailureType)` is invoked on the listener.
7. During the upload process `XMLHttpRequest` upload progress events are listened to. These update the progress on each chunk which, across all chunks, determines a net percentage of progress and this is passed through `onProgres(int)` on the listener.

With respect to a chunk start this performs the following:

1. Creates a configured instance of `XMLHttpRequest` by calling `createXMLHttpRequest(...)` (the implementation supplied by the subclass as per (2) above).
2. Attaches listeners to the `XMLHttpRequest`.
3. Obtains a configured instance of `FormData` by calling `createFormData(...)` (the implementation supplied by the subclass as per (2) above).
4. Sends the `FormData` returned by (3) or, if this is `null`, sends the chunk data directly.
