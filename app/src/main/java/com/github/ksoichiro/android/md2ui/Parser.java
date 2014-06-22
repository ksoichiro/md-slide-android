package com.github.ksoichiro.android.md2ui;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private Uri mUri;

    public Parser(final Uri uri) {
        mUri = uri;
    }

    public List<Page> parse() {
        List<Page> mPages = new ArrayList<Page>();

        List<String> lines = readFile(mUri);
        Log.i("", "url: " + mUri);
        Page page = new Page();
        boolean isCode = false;
        StringBuilder codes = null;
        for (String line : lines) {
            Log.i("", "line: " + line);
            if (line.startsWith("---")) {
                // New page
                mPages.add(page);
                page = new Page();
                continue;
            }
            Content content = new Content();
            content.content = line;
            if (isCode) {
                if (line.startsWith("```")) {
                    // Code block ends
                    content.contentType = ContentType.CODE;
                    content.content = codes.toString();
                    isCode = false;
                } else {
                    // Code block continues
                    codes.append(line);
                    codes.append("\n");
                    continue;
                }
            } else if (line.startsWith("# ")) {
                // H1
                content.contentType = ContentType.H1;
                content.content = line.substring(2);
            } else if (line.startsWith("## ")) {
                // H2
                content.contentType = ContentType.H2;
                content.content = line.substring(3);
            } else if (line.startsWith("## ")) {
                // H2
                content.contentType = ContentType.H2;
                content.content = line.substring(3);
            } else if (line.startsWith("### ")) {
                // H3
                content.contentType = ContentType.H3;
                content.content = line.substring(4);
            } else if (line.startsWith("* ") || line.startsWith("- ")) {
                // ul/li
                content.contentType = ContentType.LI;
                content.content = line.substring(2);
            } else if (line.startsWith("    * ") || line.startsWith("    - ")) {
                // ul/li(2)
                content.contentType = ContentType.LI2;
                content.content = line.substring(6);
            } else if (line.startsWith("        * ") || line.startsWith("        - ")) {
                // ul/li(2)
                content.contentType = ContentType.LI3;
                content.content = line.substring(10);
            } else if (line.startsWith("```")) {
                // code
                codes = new StringBuilder();
                isCode = true;
                continue;
            } else if (!TextUtils.isEmpty(line)) {
                // p
                content.contentType = ContentType.P;
            } else {
                // ignore
                continue;
            }
            page.contents.add(content);
        }
        mPages.add(page);

        return mPages;
    }

    private List<String> readFile(Uri uri) {
        final String charset = "UTF-8";
        List<String> lines = new ArrayList<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(uri.getPath())),
                    Charset.forName(charset)));
            String line;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lines;
    }
}
