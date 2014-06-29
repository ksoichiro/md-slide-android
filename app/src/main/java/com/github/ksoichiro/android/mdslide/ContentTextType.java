package com.github.ksoichiro.android.mdslide;

public enum ContentTextType {
    TEXT,
    CODE, // `code`
    EMPHASIS, // *em* (italic)
    EMPHASIS2, // _em_ (italic)
    STRONG, // __strong__ (bold)
    STRONG2, // **strong** (bold)
    EMPHASIS_STRONG, // *__emphasis strong__* (bold italic)
    STRONG_EMPHASIS, // __*emphasis strong*__ (bold italic)
    EMPHASIS2_STRONG2, // _**emphasis strong**_ (bold italic)
}
