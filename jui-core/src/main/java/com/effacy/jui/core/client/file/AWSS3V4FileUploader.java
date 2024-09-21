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

import java.util.List;

import com.effacy.jui.core.client.util.UID;
import com.effacy.jui.platform.util.client.StringSupport;

import elemental2.dom.DomGlobal;
import elemental2.dom.File;
import elemental2.dom.FormData;
import elemental2.dom.XMLHttpRequest;

/**
 * AWS S3 uploading (V4).
 */
public class AWSS3V4FileUploader extends FileUploader {

    /**
     * The AWS access key.
     */
    private String accessKey = null;

    /**
     * The AWS region.
     */
    private String region = null;

    /**
     * The S3 upload signature.
     */
    private String signature = null;

    /**
     * The S3 policy file.
     */
    private String policy = null;

    /**
     * The S3 key (prefix).
     */
    private String prefix = null;

    /**
     * Date stamp (in policy).
     */
    private String dateStamp;

    /**
     * The target URL for the file up-loader.
     */
    protected String url;

    /**
     * The AWS upload url.
     */
    private static final String AWS_URL = ".amazonaws.com";

    /**
     * Construct an up-loader with a target URL to upload to. The target URL
     * could simply be a context root or other type of URL prefix with the full
     * URL being constructed by adding this prefix ahead of the URL suffix
     * passed through to {@link #upload(File, String)} or
     * {@link #upload(List, String)}.
     * 
     * @param bucket
     *            the bucket being uploaded to.
     * @param policy
     *            the policy file (base64 encoded).
     * @param signature
     *            the signature of the (encoded) policy file.
     * @param accessKey
     *            the AWS access key for the AWS user that the upload will be
     *            performed under.
     * @param dateStamp
     *            the date stamp that appears in the policy.
     */
    public AWSS3V4FileUploader(String bucket, String policy, String signature, String accessKey, String dateStamp) {
        this (bucket, null, policy, signature, accessKey, dateStamp);
    }


    /**
     * See {@link #S3FileUploader(String, String, String, String)}.
     * 
     * @param bucket
     *            the bucket being uploaded to.
     * @param prefix
     *            the upload context.
     * @param policy
     *            the policy file (base64 encoded).
     * @param signature
     *            the signature of the (encoded) policy file.
     * @param accessKey
     *            the AWS access key for the AWS user that the upload will be
     *            performed under.
     * @param dateStamp
     *            the date stamp that appears in the policy.
     */
    public AWSS3V4FileUploader(String bucket, String prefix, String policy, String signature, String accessKey, String dateStamp) {
        this (bucket, prefix, policy, signature, accessKey, DomGlobal.window.location.protocol.toLowerCase ().startsWith ("https"), dateStamp);
    }


    /**
     * See {@link #S3FileUploader(String, String, String, String)}.
     * 
     * @param bucket
     *            the bucket being uploaded to.
     * @param prefix
     *            the upload context.
     * @param policy
     *            the policy file (base64 encoded).
     * @param signature
     *            the signature of the (encoded) policy file.
     * @param accessKey
     *            the AWS access key for the AWS user that the upload will be
     *            performed under.
     * @param useSSL
     *            {@code true} if SSL should be used.
     * @param dateStamp
     *            the date stamp that appears in the policy.
     */
    public AWSS3V4FileUploader(String bucket, String prefix, String policy, String signature, String accessKey, boolean useSSL, String dateStamp) {
        this (bucket, prefix, policy, signature, accessKey, useSSL, null, dateStamp);
    }


    /**
     * See {@link #S3FileUploader(String, String, String, String)}.
     * 
     * @param bucket
     *            the bucket being uploaded to.
     * @param prefix
     *            the upload context.
     * @param policy
     *            the policy file (base64 encoded).
     * @param signature
     *            the signature of the (encoded) policy file.
     * @param accessKey
     *            the AWS access key for the AWS user that the upload will be
     *            performed under.
     * @param region
     *            the base aws region.
     * @param dateStamp
     *            the date stamp that appears in the policy.
     */
    public AWSS3V4FileUploader(String bucket, String prefix, String policy, String signature, String accessKey, String region, String dateStamp) {
        this (bucket, prefix, policy, signature, accessKey, DomGlobal.window.location.protocol.toLowerCase ().startsWith ("https"), region, dateStamp);
    }


    /**
     * See {@link #S3FileUploader(String, String, String, String)}.
     * 
     * @param bucket
     *            the bucket being uploaded to.
     * @param prefix
     *            the upload context.
     * @param policy
     *            the policy file (base64 encoded).
     * @param signature
     *            the signature of the (encoded) policy file.
     * @param accessKey
     *            the AWS access key for the AWS user that the upload will be
     *            performed under.
     * @param useSSL
     *            {@code true} if SSL should be used.
     * @param region
     *            the base aws region.
     * @param dateStamp
     *            the date stamp that appears in the policy.
     */
    public AWSS3V4FileUploader(String bucket, String prefix, String policy, String signature, String accessKey, boolean useSSL, String region, String dateStamp) {
        super (0, 0);
        this.url = ((useSSL ? "https://" : "http://") + bucket + ".s3" + (StringSupport.empty (region) ? AWS_URL : ("-" + region + AWS_URL)));
        this.policy = policy;
        this.signature = signature;
        this.accessKey = accessKey;
        this.dateStamp = dateStamp;
        this.region = region;
        this.prefix = prefix;
    }

    /**
     * Update the chunk size.
     * <p>
     * If this is zero or less then no chunking will be performed.
     * 
     * @param chunkSize
     *                  the chunk size.
     * @return this uploader.
     */
    public AWSS3V4FileUploader chunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Update the maximum chunk upload concurrency.
     * <p>
     * If this is zero or less then no concurrency limits are imposed.
     * 
     * @param maxConcurrent
     *                      the concurrency limit.
     * @return this uploader.
     */
    public AWSS3V4FileUploader maxConcurrent(int maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
        return this;
    }

    /**
     * Creates a suitable (random) file name.
     * 
     * @return the name.
     */
    protected String createFileName() {
        return System.currentTimeMillis() + "-" + UID.createUID() + ".file";
    }

    @Override
    protected FileSender createSender(File file, IFileUploaderListener listener) {
        return new FileSender (file, createFileName (), listener) {

            @Override
            protected XMLHttpRequest createXMLHttpRequest(FileChunk chunk, int totalChunks, File file, String ref) {
                XMLHttpRequest xhr = new XMLHttpRequest();
                xhr.open ("post", url);
                xhr.setRequestHeader ("Cache-Control", "no-cache");
                xhr.setRequestHeader ("X-Requested-With", "XMLHttpRequest");
                xhr.setRequestHeader ("X-File-Name", file.name);
                xhr.setRequestHeader ("X-File-Type", file.type);
                xhr.setRequestHeader ("X-File-Size", Integer.toString (chunk.size()));
                xhr.setRequestHeader ("X-File-Part", Integer.toString (chunk.part ()));
                xhr.setRequestHeader ("X-File-Parts", Integer.toString (totalChunks));
                return xhr;
            }

            @Override
            protected FormData createFormData(FileChunk chunk, int totalChunks, File file, String ref) {
                FormData data = new FormData();
                if (prefix != null)
                    data.append ("key", prefix + ref);
                else
                    data.append ("key", ref);
                data.append ("acl", "private");
                // if (successCallback != null)
                //     data.append ("success_action_redirect", successCallback);
                data.append ("Content-Type", file.type);
                data.append ("X-Amz-Credential", accessKey + "/" + dateStamp + "/" + region + "/s3/aws4_request");
                data.append ("X-Amz-Algorithm", "AWS4-HMAC-SHA256");
                data.append ("X-Amz-Date", "" + dateStamp + "T000000Z");
                data.append ("Policy", policy);
                data.append ("X-Amz-Signature", signature);
                data.append ("file", chunk.data ());
                return data;
            }

        };
    }

}
