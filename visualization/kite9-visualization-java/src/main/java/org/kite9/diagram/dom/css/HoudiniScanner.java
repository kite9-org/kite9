/*

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.kite9.diagram.dom.css;

import org.apache.batik.css.parser.LexicalUnits;
import org.apache.batik.css.parser.ParseException;
import org.apache.batik.css.parser.Scanner;
import org.apache.batik.css.parser.ScannerUtilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Extends scanner so it can handle houdini properties.
 */
public class HoudiniScanner extends Scanner {

    public HoudiniScanner(Reader r) throws ParseException {
        super(r);
    }

    public HoudiniScanner(InputStream is, String encoding) {
        super(is, encoding);
    }

    public HoudiniScanner(String source) {
        super(source);
    }

    /**
     * Returns the next token.
     */
    @Override
    protected void nextToken() throws ParseException {
        try {
            switch (current) {
                case -1:
                    type = LexicalUnits.EOF;
                    return;
                case '{':
                    nextChar();
                    type = LexicalUnits.LEFT_CURLY_BRACE;
                    return;
                case '}':
                    nextChar();
                    type = LexicalUnits.RIGHT_CURLY_BRACE;
                    return;
                case '=':
                    nextChar();
                    type = LexicalUnits.EQUAL;
                    return;
                case '+':
                    nextChar();
                    type = LexicalUnits.PLUS;
                    return;
                case ',':
                    nextChar();
                    type = LexicalUnits.COMMA;
                    return;
                case ';':
                    nextChar();
                    type = LexicalUnits.SEMI_COLON;
                    return;
                case '>':
                    nextChar();
                    type = LexicalUnits.PRECEDE;
                    return;
                case '[':
                    nextChar();
                    type = LexicalUnits.LEFT_BRACKET;
                    return;
                case ']':
                    nextChar();
                    type = LexicalUnits.RIGHT_BRACKET;
                    return;
                case '*':
                    nextChar();
                    type = LexicalUnits.ANY;
                    return;
                case '(':
                    nextChar();
                    type = LexicalUnits.LEFT_BRACE;
                    return;
                case ')':
                    nextChar();
                    type = LexicalUnits.RIGHT_BRACE;
                    return;
                case ':':
                    nextChar();
                    type = LexicalUnits.COLON;
                    return;
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                case '\f':
                    do {
                        nextChar();
                    } while (ScannerUtilities.isCSSSpace((char)current));
                    type = LexicalUnits.SPACE;
                    return;
                case '/':
                    nextChar();
                    if (current != '*') {
                        type = LexicalUnits.DIVIDE;
                        return;
                    }
                    // Comment
                    nextChar();
                    start = position - 1;
                    do {
                        while (current != -1 && current != '*') {
                            nextChar();
                        }
                        do {
                            nextChar();
                        } while (current != -1 && current == '*');
                    } while (current != -1 && current != '/');
                    if (current == -1) {
                        throw new ParseException("eof",
                                reader.getLine(),
                                reader.getColumn());
                    }
                    nextChar();
                    type = LexicalUnits.COMMENT;
                    return;
                case '\'': // String1
                    type = string1();
                    return;
                case '"': // String2
                    type = string2();
                    return;
                case '<':
                    nextChar();
                    if (current != '!') {
                        throw new ParseException("character",
                                reader.getLine(),
                                reader.getColumn());
                    }
                    nextChar();
                    if (current == '-') {
                        nextChar();
                        if (current == '-') {
                            nextChar();
                            type = LexicalUnits.CDO;
                            return;
                        }
                    }
                    throw new ParseException("character",
                            reader.getLine(),
                            reader.getColumn());
                case '-':
                    nextChar();
                    if (current == '-') {
                        nextChar();
                        if (current == '>') {
                            nextChar();
                            type = LexicalUnits.CDC;
                            return;
                        } else if (ScannerUtilities.isCSSNameCharacter((char)current)) {
                            scanIdentifierOrFunctionHoudini();
                            return;
                        }
                    } else if ((ScannerUtilities.isCSSNameCharacter((char)current))
                            && (!Character.isDigit((char)current))) {
                        scanIdentifierOrFunctionHoudini();
                        return;
                    }

                    type = LexicalUnits.MINUS;
                    return;

                case '|':
                    nextChar();
                    if (current == '=') {
                        nextChar();
                        type = LexicalUnits.DASHMATCH;
                        return;
                    }
                    throw new ParseException("character",
                            reader.getLine(),
                            reader.getColumn());
                case '~':
                    nextChar();
                    if (current == '=') {
                        nextChar();
                        type = LexicalUnits.INCLUDES;
                        return;
                    }
                    throw new ParseException("character",
                            reader.getLine(),
                            reader.getColumn());
                case '#':
                    nextChar();
                    if (ScannerUtilities.isCSSNameCharacter((char)current)) {
                        start = position - 1;
                        do {
                            nextChar();
                            while (current == '\\') {
                                nextChar();
                                escape();
                            }
                        } while (current != -1 &&
                                ScannerUtilities.isCSSNameCharacter
                                        ((char)current));
                        type = LexicalUnits.HASH;
                        return;
                    }
                    throw new ParseException("character",
                            reader.getLine(),
                            reader.getColumn());
                case '@':
                    nextChar();
                    switch (current) {
                        case 'c':
                        case 'C':
                            start = position - 1;
                            if (isEqualIgnoreCase(nextChar(), 'h') &&
                                    isEqualIgnoreCase(nextChar(), 'a') &&
                                    isEqualIgnoreCase(nextChar(), 'r') &&
                                    isEqualIgnoreCase(nextChar(), 's') &&
                                    isEqualIgnoreCase(nextChar(), 'e') &&
                                    isEqualIgnoreCase(nextChar(), 't')) {
                                nextChar();
                                type = LexicalUnits.CHARSET_SYMBOL;
                                return;
                            }
                            break;
                        case 'f':
                        case 'F':
                            start = position - 1;
                            if (isEqualIgnoreCase(nextChar(), 'o') &&
                                    isEqualIgnoreCase(nextChar(), 'n') &&
                                    isEqualIgnoreCase(nextChar(), 't') &&
                                    isEqualIgnoreCase(nextChar(), '-') &&
                                    isEqualIgnoreCase(nextChar(), 'f') &&
                                    isEqualIgnoreCase(nextChar(), 'a') &&
                                    isEqualIgnoreCase(nextChar(), 'c') &&
                                    isEqualIgnoreCase(nextChar(), 'e')) {
                                nextChar();
                                type = LexicalUnits.FONT_FACE_SYMBOL;
                                return;
                            }
                            break;
                        case 'i':
                        case 'I':
                            start = position - 1;
                            if (isEqualIgnoreCase(nextChar(), 'm') &&
                                    isEqualIgnoreCase(nextChar(), 'p') &&
                                    isEqualIgnoreCase(nextChar(), 'o') &&
                                    isEqualIgnoreCase(nextChar(), 'r') &&
                                    isEqualIgnoreCase(nextChar(), 't')) {
                                nextChar();
                                type = LexicalUnits.IMPORT_SYMBOL;
                                return;
                            }
                            break;
                        case 'm':
                        case 'M':
                            start = position - 1;
                            if (isEqualIgnoreCase(nextChar(), 'e') &&
                                    isEqualIgnoreCase(nextChar(), 'd') &&
                                    isEqualIgnoreCase(nextChar(), 'i') &&
                                    isEqualIgnoreCase(nextChar(), 'a')) {
                                nextChar();
                                type = LexicalUnits.MEDIA_SYMBOL;
                                return;
                            }
                            break;
                        case 'p':
                        case 'P':
                            start = position - 1;
                            if (isEqualIgnoreCase(nextChar(), 'a') &&
                                    isEqualIgnoreCase(nextChar(), 'g') &&
                                    isEqualIgnoreCase(nextChar(), 'e')) {
                                nextChar();
                                type = LexicalUnits.PAGE_SYMBOL;
                                return;
                            }
                            break;
                        default:
                            if (!ScannerUtilities.isCSSIdentifierStartCharacter
                                    ((char)current)) {
                                throw new ParseException("identifier.character",
                                        reader.getLine(),
                                        reader.getColumn());
                            }
                            start = position - 1;
                    }
                    do {
                        nextChar();
                        while (current == '\\') {
                            nextChar();
                            escape();
                        }
                    } while (current != -1 &&
                            ScannerUtilities.isCSSNameCharacter((char)current));
                    type = LexicalUnits.AT_KEYWORD;
                    return;
                case '!':
                    do {
                        nextChar();
                    } while (current != -1 &&
                            ScannerUtilities.isCSSSpace((char)current));
                    if (isEqualIgnoreCase(current, 'i') &&
                            isEqualIgnoreCase(nextChar(), 'm') &&
                            isEqualIgnoreCase(nextChar(), 'p') &&
                            isEqualIgnoreCase(nextChar(), 'o') &&
                            isEqualIgnoreCase(nextChar(), 'r') &&
                            isEqualIgnoreCase(nextChar(), 't') &&
                            isEqualIgnoreCase(nextChar(), 'a') &&
                            isEqualIgnoreCase(nextChar(), 'n') &&
                            isEqualIgnoreCase(nextChar(), 't')) {
                        nextChar();
                        type = LexicalUnits.IMPORTANT_SYMBOL;
                        return;
                    }
                    if (current == -1) {
                        throw new ParseException("eof",
                                reader.getLine(),
                                reader.getColumn());
                    } else {
                        throw new ParseException("character",
                                reader.getLine(),
                                reader.getColumn());
                    }
                case '0': case '1': case '2': case '3': case '4':
                case '5': case '6': case '7': case '8': case '9':
                    type = number();
                    return;
                case '.':
                    switch (nextChar()) {
                        case '0': case '1': case '2': case '3': case '4':
                        case '5': case '6': case '7': case '8': case '9':
                            type = dotNumber();
                            return;
                        default:
                            type = LexicalUnits.DOT;
                            return;
                    }
                case 'u':
                case 'U':
                    nextChar();
                    switch (current) {
                        case '+':
                            boolean range = false;
                            for (int i = 0; i < 6; i++) {
                                nextChar();
                                switch (current) {
                                    case '?':
                                        range = true;
                                        break;
                                    default:
                                        if (range &&
                                                !ScannerUtilities.isCSSHexadecimalCharacter
                                                        ((char)current)) {
                                            throw new ParseException("character",
                                                    reader.getLine(),
                                                    reader.getColumn());
                                        }
                                }
                            }
                            nextChar();
                            if (range) {
                                type = LexicalUnits.UNICODE_RANGE;
                                return;
                            }
                            if (current == '-') {
                                nextChar();
                                if (!ScannerUtilities.isCSSHexadecimalCharacter
                                        ((char)current)) {
                                    throw new ParseException("character",
                                            reader.getLine(),
                                            reader.getColumn());
                                }
                                nextChar();
                                if (!ScannerUtilities.isCSSHexadecimalCharacter
                                        ((char)current)) {
                                    type = LexicalUnits.UNICODE_RANGE;
                                    return;
                                }
                                nextChar();
                                if (!ScannerUtilities.isCSSHexadecimalCharacter
                                        ((char)current)) {
                                    type = LexicalUnits.UNICODE_RANGE;
                                    return;
                                }
                                nextChar();
                                if (!ScannerUtilities.isCSSHexadecimalCharacter
                                        ((char)current)) {
                                    type = LexicalUnits.UNICODE_RANGE;
                                    return;
                                }
                                nextChar();
                                if (!ScannerUtilities.isCSSHexadecimalCharacter
                                        ((char)current)) {
                                    type = LexicalUnits.UNICODE_RANGE;
                                    return;
                                }
                                nextChar();
                                if (!ScannerUtilities.isCSSHexadecimalCharacter
                                        ((char)current)) {
                                    type = LexicalUnits.UNICODE_RANGE;
                                    return;
                                }
                                nextChar();
                                type = LexicalUnits.UNICODE_RANGE;
                                return;
                            }
                        case 'r':
                        case 'R':
                            nextChar();
                            switch (current) {
                                case 'l':
                                case 'L':
                                    nextChar();
                                    switch (current) {
                                        case '(':
                                            do {
                                                nextChar();
                                            } while (current != -1 &&
                                                    ScannerUtilities.isCSSSpace
                                                            ((char)current));
                                            switch (current) {
                                                case '\'':
                                                    string1();
                                                    blankCharacters += 2;
                                                    while (current != -1 &&
                                                            ScannerUtilities.isCSSSpace
                                                                    ((char)current)) {
                                                        blankCharacters++;
                                                        nextChar();
                                                    }
                                                    if (current == -1) {
                                                        throw new ParseException
                                                                ("eof",
                                                                        reader.getLine(),
                                                                        reader.getColumn());
                                                    }
                                                    if (current != ')') {
                                                        throw new ParseException
                                                                ("character",
                                                                        reader.getLine(),
                                                                        reader.getColumn());
                                                    }
                                                    nextChar();
                                                    type = LexicalUnits.URI;
                                                    return;
                                                case '"':
                                                    string2();
                                                    blankCharacters += 2;
                                                    while (current != -1 &&
                                                            ScannerUtilities.isCSSSpace
                                                                    ((char)current)) {
                                                        blankCharacters++;
                                                        nextChar();
                                                    }
                                                    if (current == -1) {
                                                        throw new ParseException
                                                                ("eof",
                                                                        reader.getLine(),
                                                                        reader.getColumn());
                                                    }
                                                    if (current != ')') {
                                                        throw new ParseException
                                                                ("character",
                                                                        reader.getLine(),
                                                                        reader.getColumn());
                                                    }
                                                    nextChar();
                                                    type = LexicalUnits.URI;
                                                    return;
                                                case ')':
                                                    throw new ParseException("character",
                                                            reader.getLine(),
                                                            reader.getColumn());
                                                default:
                                                    if (!ScannerUtilities.isCSSURICharacter
                                                            ((char)current)) {
                                                        throw new ParseException
                                                                ("character",
                                                                        reader.getLine(),
                                                                        reader.getColumn());
                                                    }
                                                    start = position - 1;
                                                    do {
                                                        nextChar();
                                                    } while (current != -1 &&
                                                            ScannerUtilities.isCSSURICharacter
                                                                    ((char)current));
                                                    blankCharacters++;
                                                    while (current != -1 &&
                                                            ScannerUtilities.isCSSSpace
                                                                    ((char)current)) {
                                                        blankCharacters++;
                                                        nextChar();
                                                    }
                                                    if (current == -1) {
                                                        throw new ParseException
                                                                ("eof",
                                                                        reader.getLine(),
                                                                        reader.getColumn());
                                                    }
                                                    if (current != ')') {
                                                        throw new ParseException
                                                                ("character",
                                                                        reader.getLine(),
                                                                        reader.getColumn());
                                                    }
                                                    nextChar();
                                                    type = LexicalUnits.URI;
                                                    return;
                                            }
                                    }
                            }
                    }
                    while (current != -1 &&
                            ScannerUtilities.isCSSNameCharacter((char)current)) {
                        nextChar();
                    }
                    if (current == '(') {
                        nextChar();
                        type = LexicalUnits.FUNCTION;
                        return;
                    }
                    type = LexicalUnits.IDENTIFIER;
                    return;
                default:
                    if (current == '\\') {
                        do {
                            nextChar();
                            escape();
                        } while(current == '\\');
                    } else if (!ScannerUtilities.isCSSIdentifierStartCharacter
                            ((char)current)) {
                        nextChar();
                        throw new ParseException("identifier.character",
                                reader.getLine(),
                                reader.getColumn());
                    }
                    scanIdentifierOrFunctionHoudini();
                    return;
            }
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    private void scanIdentifierOrFunctionHoudini() throws IOException {
        // Identifier
        while ((current != -1) &&
                ScannerUtilities.isCSSNameCharacter((char)current)) {
            nextChar();
            while (current == '\\') {
                nextChar();
                escape();
            }
        }
        if (current == '(') {
            nextChar();
            type = LexicalUnits.FUNCTION;
            return;
        }
        type = LexicalUnits.IDENTIFIER;
    }


}