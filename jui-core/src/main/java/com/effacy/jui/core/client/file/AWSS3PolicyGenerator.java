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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.ibm.icu.util.Calendar;

/**
 * This is used specifically to generate a policy file and the associate policy
 * and signature strings that is compliant with {@link AWSS3V4FileUploader}.
 * 
 * @author Jeremy Buckley
 */
public class AWSS3PolicyGenerator {

    /**
     * Invoked to generate a policy and signature that is consistent for use
     * with this uploader.
     * 
     * @param args
     *            the arguments to pass.
     * @throws Exception
     *             On error.
     */
    public static void main(String [] args) throws Exception {
        boolean help = false;
        boolean v2 = false;

        // Check for a v2 signature.
        if (argumentExists (args, "--v2"))
            v2 = true;

        // Get the AWS secret.
        String secret = findArgument (args, "--secret");
        if (secret == null)
            help = true;

        // Get the AWS key.
        String key = findArgument (args, "--key");
        if ((key == null) && !v2)
            help = true;

        // Get the AWS region (default to eu-west-2 for no reason in
        // particular).
        String region = findArgument (args, "--region");
        if (region == null)
            region = "eu-west-2";

        // Expiration date. In format YYYY-MM-DDT00:00:00Z. The passed argument is a
        // year (i.e. 2026).
        String expiration = findArgument (args, "--expiration");
        if (expiration == null) {
            // Default to two years from now.
            int year = Calendar.getInstance ().get (Calendar.YEAR);
            year += 2;
            expiration = Integer.toString (year);
        }
        expiration += "-01-01T00:00:00Z";

        // Establish the policy file.
        StringWriter policyFileContents = new StringWriter ();
        if (findArgument (args, "--policy-file") != null) {
            BufferedReader reader = new BufferedReader (new FileReader (new File (findArgument (args, "--policy-file"))));
            String line = null;
            while ((line = reader.readLine ()) != null)
                policyFileContents.append (line);
            reader.close ();
        } else {
            String bucket = findArgument (args, "--bucket");
            String redirect = findArgument (args, "--redirect");
            String startsWith = findArgument (args, "--starts-with");
            if (startsWith == null)
                startsWith = "";
            String contentLength = findArgument (args, "--content-length");
            if (contentLength == null)
                contentLength = "524288000";
            if (bucket == null) {
                help = true;
            } else {
                policyFileContents.append ("{\n");
                policyFileContents.append (" \"expiration\": \"" + expiration + "\",\n");
                policyFileContents.append (" \"conditions\": [\n");
                policyFileContents.append ("  {\"bucket\": \"" + bucket + "\"},\n");
                policyFileContents.append ("  [\"starts-with\", \"$key\", \"" + startsWith + "\"],\n");
                policyFileContents.append ("  {\"acl\": \"private\"},\n");
                if (redirect != null)
                    policyFileContents.append ("  {\"success_action_redirect\": \"" + redirect + "\"},\n");
                policyFileContents.append ("  [\"starts-with\", \"$Content-Type\", \"\"],\n");
                if (v2)
                    policyFileContents.append ("  [\"content-length-range\", 0, " + contentLength + "]\n");
                else
                    policyFileContents.append ("  [\"content-length-range\", 0, " + contentLength + "],\n");
                if (!v2) {
                    policyFileContents.append ("  {\"x-amz-credential\": \"" + key + "/20151229/" + region + "/s3/aws4_request\"},\n");
                    policyFileContents.append ("  {\"x-amz-algorithm\": \"AWS4-HMAC-SHA256\"},\n");
                    policyFileContents.append ("  {\"x-amz-date\": \"20151229T000000Z\" }\n");
                }
                policyFileContents.append (" ]\n");
                policyFileContents.append ("}");
            }
        }

        if (help) {
            System.out.println ("Usage: java -jar <jar> --secret=<aws-secret> <file-spec|policy-spec>");
            System.out.println ();
            System.out.println ("file-spec:");
            System.out.println ("  --policy-file=<file>");
            System.out.println ();
            System.out.println ("policy-spec:");
            System.out.println ("  --bucket=<s3-bucket> [--redirect=<success-redirect-url>] [--starts-with=<file-prefix>] [--content-length=<max-size>]");
            System.exit (1);
        }

        System.out.println ("File:");
        System.out.println (policyFileContents.toString ());

        // Create the policy.
        String policy = Base64.getEncoder ().encodeToString (policyFileContents.toString ().getBytes ("UTF-8")).replaceAll ("\n", "").replaceAll ("\r", "");
        System.out.println ("Policy:    " + policy);

        // Create the signature.
        Mac hmac = Mac.getInstance ("HmacSHA1");
        if (secret == null)
            return;
        hmac.init (new SecretKeySpec (secret.getBytes ("UTF-8"), "HmacSHA1"));
        String signature = Base64.getEncoder ().encodeToString (hmac.doFinal (policy.getBytes ("UTF-8"))).replaceAll ("\n", "");
        System.out.println ("Signature: " + signature);
    }


    /**
     * Extracts an argument from a list of arguments.
     * 
     * @param args
     *            the list of arguments.
     * @param name
     *            the name of the argument.
     * @return The value of the argument.
     */
    private static String findArgument(String [] args, String name) {
        name = name + "=";
        for (String arg : args) {
            if (arg.startsWith (name))
                return arg.substring (name.length ());
        }
        return null;
    }


    /**
     * Determines if an argument (flag) exists.
     * 
     * @param args
     *            the list of arguments.
     * @param name
     *            the name of the argument.
     * @return {@code true} if it is present.
     */
    private static boolean argumentExists(String [] args, String name) {
        for (String arg : args) {
            if (arg.startsWith (name))
                return true;
        }
        return false;
    }

}
