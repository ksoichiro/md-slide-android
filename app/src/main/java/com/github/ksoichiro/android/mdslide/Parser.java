package com.github.ksoichiro.android.mdslide;

import android.content.res.AssetManager;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    private InputStream mIn;
    private Uri mUri;

    public Parser(final InputStream in) {
        mIn = in;
    }

    public Parser(final Uri uri) {
        mUri = uri;
    }

    public List<Page> parse() {
        List<Page> mPages = new ArrayList<Page>();

        InputStream in;
        if (mUri != null) {
            try {
                in = new FileInputStream(new File(mUri.getPath()));
            } catch (IOException e) {
                return mPages;
            }
        } else if (mIn != null) {
            in = mIn;
        } else {
            return mPages;
        }
        List<String> lines = readFile(in);
        Log.i("", "url: " + mUri);
        Page page = new Page();
        boolean isCode = false;
        boolean isQuote = false;
        StringBuilder codes = null;
        StringBuilder quotes = null;
        int pageNumber = 1;
        for (String line : lines) {
            Log.i("", "line: " + line);
            Content content = new Content();

            // Quit previous mode
            if (isQuote && !line.startsWith("> ")) {
                // Quote block ends
                content.contentType = ContentType.QUOTE;
                parseContentText(content, quotes.toString());
                isQuote = false;
                page.contents.add(content);
            }

            if (line.startsWith("---")) {
                // New page
                page.number = pageNumber;
                mPages.add(page);
                pageNumber++;
                page = new Page();
                continue;
            }

            if (isCode) {
                if (line.startsWith("```")) {
                    // Code block ends
                    content.contentType = ContentType.CODE;
                    content.addContentText(new ContentText(codes.toString()));
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
                parseContentText(content, line.substring(2));
            } else if (line.startsWith("## ")) {
                // H2
                content.contentType = ContentType.H2;
                parseContentText(content, line.substring(3));
            } else if (line.startsWith("## ")) {
                // H2
                content.contentType = ContentType.H2;
                parseContentText(content, line.substring(3));
            } else if (line.startsWith("### ")) {
                // H3
                content.contentType = ContentType.H3;
                parseContentText(content, line.substring(4));
            } else if (line.startsWith("* ") || line.startsWith("- ")) {
                // ul/li
                content.contentType = ContentType.LI;
                parseContentText(content, line.substring(2));
            } else if (line.startsWith("    * ") || line.startsWith("    - ")) {
                // ul/li(2)
                content.contentType = ContentType.LI2;
                parseContentText(content, line.substring(6));
            } else if (line.startsWith("        * ") || line.startsWith("        - ")) {
                // ul/li(2)
                content.contentType = ContentType.LI3;
                parseContentText(content, line.substring(10));
            } else if (line.startsWith("```")) {
                // code
                codes = new StringBuilder();
                isCode = true;
                continue;
            } else if (line.startsWith("> ")) {
                // quote
                if (!isQuote) {
                    isQuote = true;
                    quotes = new StringBuilder();
                }
                quotes.append(line.substring(2));
                continue;
            } else if (line.startsWith("![")) {
                Pattern p = Pattern.compile("^!\\[([^\\]]*)\\]\\(([^\\)]*)\\)");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    String paramsRaw = m.group(1);
                    String[] params = new String[0];
                    if (paramsRaw != null) {
                        String[] paramsSplit = paramsRaw.split(",");
                        params = new String[paramsSplit.length];
                        for (int i = 0; i < paramsSplit.length; i++) {
                            String param = paramsSplit[i];
                            params[i] = param.trim();
                        }
                    }
                    String url = m.group(2);
                    Log.v("", "Image: params: " + paramsRaw);
                    Log.v("", "Image: url: " + url);
                    content.contentType = ContentType.IMG;
                    content.initContentTexts();
                    content.attributes.put("params", params);
                    content.attributes.put("url", url);
                } else {
                    continue;
                }
            } else if (!TextUtils.isEmpty(line)) {
                // p
                content.contentType = ContentType.P;
                parseContentText(content, line);
            } else {
                // ignore
                continue;
            }
            page.contents.add(content);
        }

        // Flush quotes
        if (quotes != null && 0 < quotes.length()) {
            Content content = new Content();
            content.contentType = ContentType.QUOTE;
            parseContentText(content, quotes.toString());
            page.contents.add(content);
        }

        page.number = pageNumber;
        mPages.add(page);

        return mPages;
    }

    private void parseContentText(final Content content, String target) {
        StringBuilder buffer = new StringBuilder();
        ContentTextType currentContentTextType = ContentTextType.TEXT;
        for (String rest = target; 0 < rest.length(); rest = rest.substring(1)) {
            if (rest.startsWith("`")) {
                buffer = putContentText(buffer, content, currentContentTextType);
                switch (currentContentTextType) {
                    case CODE:
                        currentContentTextType = ContentTextType.TEXT;
                        break;
                    default:
                        currentContentTextType = ContentTextType.CODE;
                        break;
                }
                continue;
            } else if (currentContentTextType == ContentTextType.CODE) {
                buffer.append(rest.substring(0, 1));
                continue;
            }

            if (rest.startsWith("**")) {
                buffer = putContentText(buffer, content, currentContentTextType);
                switch (currentContentTextType) {
                    case STRONG2:
                        currentContentTextType = ContentTextType.TEXT;
                        break;
                    case EMPHASIS2:
                        currentContentTextType = ContentTextType.EMPHASIS2_STRONG2;
                        break;
                    case EMPHASIS2_STRONG2:
                        currentContentTextType = ContentTextType.EMPHASIS2;
                        break;
                    default:
                        currentContentTextType = ContentTextType.STRONG2;
                        break;
                }
                rest = rest.substring(1); // '*'
            } else if (rest.startsWith("*")) {
                buffer = putContentText(buffer, content, currentContentTextType);
                switch (currentContentTextType) {
                    case EMPHASIS:
                    case EMPHASIS_STRONG:
                        currentContentTextType = ContentTextType.TEXT;
                        break;
                    case STRONG:
                        currentContentTextType = ContentTextType.STRONG_EMPHASIS;
                        break;
                    case STRONG_EMPHASIS:
                        currentContentTextType = ContentTextType.STRONG;
                        break;
                    default:
                        currentContentTextType = ContentTextType.EMPHASIS;
                        break;
                }
            } else if (rest.startsWith("__")) {
                buffer = putContentText(buffer, content, currentContentTextType);
                switch (currentContentTextType) {
                    case STRONG:
                    case STRONG_EMPHASIS:
                        currentContentTextType = ContentTextType.TEXT;
                        break;
                    case EMPHASIS:
                        currentContentTextType = ContentTextType.EMPHASIS_STRONG;
                        break;
                    case EMPHASIS_STRONG:
                        currentContentTextType = ContentTextType.EMPHASIS;
                        break;
                    default:
                        currentContentTextType = ContentTextType.STRONG;
                        break;
                }
                rest = rest.substring(1); // '_'
            } else if (rest.startsWith("_")) {
                buffer = putContentText(buffer, content, currentContentTextType);
                switch (currentContentTextType) {
                    case EMPHASIS2:
                        currentContentTextType = ContentTextType.TEXT;
                        break;
                    default:
                        currentContentTextType = ContentTextType.EMPHASIS2;
                        break;
                }
            } else {
                buffer.append(rest.substring(0, 1));
            }
        }
        if (0 < buffer.length()) {
            content.addContentText(new ContentText(buffer.toString(), currentContentTextType));
        }
    }

    private StringBuilder putContentText(final StringBuilder buffer, final Content content, final ContentTextType currentContentTextType) {
        if (0 < buffer.length()) {
            content.addContentText(new ContentText(buffer.toString(), currentContentTextType));
        }
        return new StringBuilder();
    }

    private List<String> readFile(InputStream inStream) {
        final String charset = "UTF-8";
        List<String> lines = new ArrayList<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    inStream,
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
