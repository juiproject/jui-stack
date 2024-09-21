/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.core.client.file;

import java.util.function.Function;

import com.effacy.jui.core.client.file.IFileUploader.IFileUploaderListener.FailureType;

import elemental2.dom.File;
import elemental2.dom.FormData;
import elemental2.dom.XMLHttpRequest;

/**
 * Construct with a URL that will retrieve a signed URL to PUT to. Note that the
 * PUT content will be typed (with the <code>Content-Type</code> header) as
 * <code>application/octet-stream</code> (though this can be changed by setting
 * {@link #CONTENT_TYPE}).
 * <p>
 * To set a bucket and service URL (accurate as of April 2024):
 * <ol>
 * <li>Create a bucket (the target to upload to).</li>
 * <li>Create a CORS configuration that allows access to the bucket from the
 * desired referrer locations (i.e. localhost or production server).</li>
 * <li>Create a service account that is granted the
 * <code>roles/storage.legacyBucketWriter</code> role to the bucket.</li>
 * <li>Obtain credentials to the account for the application (i.e. key or
 * delegated workload). This is used in the next step to establish credentials
 * to a <code>Storage</code> instance (as part of the GCP cloud storage
 * API).</li>
 * <li>On the service URL expose an endpoint that creates a signed URL to the
 * aforementioned bucket and suitable (i.e. randomly named) target file. This
 * should be V4 signed (see
 * <code>Storage.SignUrlOption.withV4Signature ()</code>) and granted PUT (see
 * <code>Storage.SignUrlOption.httpMethod (HttpMethod.PUT)</code>).</li>
 * <li>Passs the service URL to an instance of the file uploader and it will
 * obtain a signed URL and upload (PUT) the file to that signed URL. The
 * associated reference is the bucket and filename extracted from the signed
 * URL.</li>
 * </ol>
 * Here is an example CORS configuration:
 * <tt>
 * [
 *   {
 *     "origin": ["http://localhost"],
 *     "method": ["PUT","OPTIONS"],
 *     "responseHeader": ["Content-Type","X-Goog-Content-Length-Range"],
 *     "maxAgeSeconds": 3600
 *   }
 * ]
 * </tt>
 * Here is some code for generating a signed URL:
 * <tt>
 * String credentialsFile = ...;
 * GoogleCredentials credentials =
 *   GoogleCredentials.fromStream (new FileInputStream (credentialsFile));
 * StorageOptions storageOptions = StorageOptions.newBuilder ()
 *   .setCredentials (credentials).build ();
 * Storage storage = storageOptions.getService ();
 * String file = UUID.randomUUID ().toString ();
 * Map<String, String> extensionHeaders = new HashMap<> ();
 * extensionHeaders.put ("Content-Type", "application/octet-stream");
 * extensionHeaders.put ("X-Goog-Content-Length-Range", "0," + 10L*1024L*1024L);
 * URL signedUrl = storage.signUrl (
 *   BlobInfo.newBuilder (uploaderBucket, file).build (),
 *   15,
 *   TimeUnit.MINUTES,
 *   Storage.SignUrlOption.httpMethod (HttpMethod.PUT),
 *   Storage.SignUrlOption.withExtHeaders (extensionHeaders),
 *   Storage.SignUrlOption.withV4Signature ());
 * </tt>
 * Note the <code>extensionHeaders</code>. The first is the
 * <code>Content-Type</code> which is passed back as a header by this
 * implementation (as noted above). If this is not present the a 400 is
 * returned. The second <code>X-Goog-Content-Length-Range</code> is optional but
 * can be used to limit the file size (as a range, sometimes you want to set the
 * lower end of the scale to 1). If you use this then you need to call
 * {@link #contentLengthRange(long, long)} with the same values as declared when
 * building the signed URL. This is passed back as a header and needs to match
 * that on the signed URL. Separately GCS will validate the file size and will
 * reject any file that falls outside these bounds. If you do use this you
 * should separately validate files before they get passed to the uploader (i.e.
 * when using a file upload control) as the error condition can be quite opaque
 * (just the response code is returned).
 */
public class GCPSignedUrlFileUploader extends FileUploader {

    /**
     * Used to extract the bucket and file name from the signed URL.
     */
    public static String SIGNED_URL_PREFIX = "storage.googleapis.com/";

    /**
     * The content type to send back.
     */
    public static String CONTENT_TYPE = "application/octet-stream";

    /**
     * The service URL to obtain the signed URL from.
     */
    private String url;

    /**
     * Timeout for the service URL.
     */
    private int timeout;

    /**
     * See {@link #contentType(String)}.
     */
    private String contentType = CONTENT_TYPE;

    /**
     * See {@link #contentLengthRange(long, long)}.
     */
    private String contentLengthRange;

    /**
     * See {@link #referenceProvider(Function)}.
     */
    private Function<String,String> referenceProvider;

    /**
     * Constructs an instance of the uploader with initial configuration.
     * 
     * @param serviceUrl
     *                   the service url that returns a signed URL as the upload
     *                   target.
     * @param timeout
     *                   the timeout to apply to retrieving a signed URL from the
     *                   service URL (in ms).
     */
    public GCPSignedUrlFileUploader(String serviceUrl, int timeout) {
        super (0, 0);
        this.url = serviceUrl;
        this.timeout = timeout;
    }

    /**
     * Assigns a content type. This is passed back as the {@code Content-Type}
     * header (so also needs to be included in the signed URL).
     * <p>
     * The default is to return {@link #CONTENT_TYPE} but this is overridden with
     * this value. Setting to {@code null} sends no header.
     * 
     * @param contentType
     *                    the content type value.
     * @return this uploader instance
     */
    public GCPSignedUrlFileUploader contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Assigns the minimum and maximum content length range when the signed URL
     * encodes a {@code X-Goog-Content-Length-Range} header. This is passed through
     * on the request for validation.
     * 
     * @param min
     *            the minimum length.
     * @param max
     *            the maximum length.
     * @return this uploader instance.
     */
    public GCPSignedUrlFileUploader contentLengthRange(long min, long max) {
        contentLengthRange = min + "," + max;
        return this;
    }

    /**
     * Assigns a mapper that takes the signed URL and resolves it to a reference.
     * <p>
     * The default used the bucket and file path as the reference (see
     * {@link #extractBucketAndFile(String)}).
     * 
     * @param referenceProvider
     *                          the mapping function.
     * @return this uploader instance.
     */
    public GCPSignedUrlFileUploader referenceProvider(Function<String,String> referenceProvider) {
        this.referenceProvider = referenceProvider;
        return this;
    }

    @Override
    protected FileSender createSender(File file, IFileUploaderListener listener) {
        return new FileSender(file, null, listener) {

            protected String signedUrl;

            @Override
            public void start() {
                // Here we need to retrieve the signed URL by calling the service URL.
                XMLHttpRequest r = new XMLHttpRequest ();
                r.open ("GET", url);
                r.timeout = timeout;
                r.ontimeout = e -> {
                    fail (FailureType.TIMEOUT);
                };
                r.onload = e -> {
                    if (r.status == 200) {
                        signedUrl = r.responseText;
                        ref = (referenceProvider != null) ? referenceProvider.apply (signedUrl) : extractBucketAndFile (signedUrl);
                        super.start ();
                    } else
                        fail (FailureType.ERROR);
                };
                r.onerror = e -> {
                    fail (FailureType.ERROR);
                    return null;
                };
                r.onabort = e -> {
                    fail (FailureType.ABORT);
                };
                r.send ();
            }

            @Override
            protected XMLHttpRequest createXMLHttpRequest(FileChunk chunk, int totalChunks, File file, String ref) {
                XMLHttpRequest xhr = new XMLHttpRequest ();
                xhr.open ("PUT", signedUrl, true);
                if (contentType != null)
                    xhr.setRequestHeader ("Content-Type", contentType);
                if (contentLengthRange != null)
                    xhr.setRequestHeader ("X-Goog-Content-Length-Range", contentLengthRange);
                return xhr;
            }

            @Override
            protected FormData createFormData(FileChunk chunk, int totalChunks, File file, String ref) {
                // We don't use form data.
                return null;
                // FormData data = new FormData();
                // data.append ("Content-Type", CONTENT_TYPE);
                // data.append ("file", chunk.data ());
                // return data;
            }
        };
    }

    /**
     * Given a signed URL this extracts the bucket and file component.
     * 
     * @param signedUrl
     *                  the sgned URL.
     * @return the bucked and file.
     */
    public static String extractBucketAndFile(String signedUrl) {
        String ref = signedUrl;
        int i = ref.indexOf ('?');
        if (i > 0)
            ref = ref.substring (0, i);
        i = ref.indexOf (SIGNED_URL_PREFIX);
        if (i >= 0)
            ref = ref.substring (i + SIGNED_URL_PREFIX.length ());
        return ref;
    }

}
