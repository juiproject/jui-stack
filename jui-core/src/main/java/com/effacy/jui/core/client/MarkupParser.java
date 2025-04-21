/*******************************************************************************
 * Copyright 2025 Jeremy Buckley
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
package com.effacy.jui.core.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A very simple markup parser that converts a string containing basic markup
 * into a structure that can be rendered.
 */
public class MarkupParser {

    /**
     * Represents a block of text with formatting applied.
     */
    public static record Block(String text, boolean bold, boolean italic) {

        /**
         * Determines if this represents text that has no formatting.
         * 
         * @return {@code true} if so.
         */
        public boolean isPlain() {
            return !bold && !italic;
        }
    }

    /**
     * Parses a block of text into formatted text.
     * 
     * @param text
     *             the text to parse.
     * @return the formatted text.
     */
    public static List<Block> parse(String text) {
        List<Block> results = new ArrayList<>();
        if (text == null)
            return results;
        if (!text.contains("*")) {
            results.add (new Block(text, false, false));
            return results;
        }
        new Scanner(b -> results.add(b)).process(text);
        return results;
    }

    /**
     * A simple scanner that employs a state machine to process an input string with
     * simple markup using "*" as a special token.
     */
    static class Scanner {

        /**
         * To emit blocks.
         */
        private Consumer<Block> appender;

        /**
         * Currently accumulated string that represents the contents of the next block.
         */
        private String str;

        /**
         * Construct with an appender.
         * 
         * @param appender
         *                 appender to emit blocks to.
         */
        private Scanner(Consumer<Block> appender) {
            this.appender = appender;
        }

        /**
         * Procsses the given markup text.
         * 
         * @param text
         *             the text formatted with markup.
         */
        public void process(String text) {
            //System.out.println("-----");
            //System.out.println("\"" + text + "\" -> P");
            str = "";
            State state = State.P;
            for (int i = 0, len = text.length(); i < len; i++) {
                char ch = text.charAt(i);
                state = state.transition(this, ch);
            }
            state.end(this);
        }

        /**
         * To add a character to the current block text.
         * 
         * @param c
         *          the character to add.
         */
        private void add(char c) {
            str += c;
        }

        /**
         * Flushes the current block text as a block with the given formatting.
         * 
         * @param bold
         *               bold formatting.
         * @param italic
         *               italic formatting.
         */
        private void flush(boolean bold, boolean italic) {
            if (str.length() > 0)
                appender.accept(new Block(str, bold, italic));
            str = "";
        }

        /**
         * Simple state machine for processing a string as a sequence of characters with
         * "*" being a special token.
         */
        enum State {
            P(false,false),
            B(true,false),
            BSTR(true,false),
            BSTR_S(true,false),
            I(false,true),
            ISTR(false,true),
            IB(true,true),
            IBSTR(true,true)
            ;

            static {
                // Setup the state transitions. Parameters are:
                // flushOnStar, stateOnStar, stateOnChar
                P.init(true, B, P);
                B.init(false, I, BSTR);
                BSTR.init(true, BSTR_S, BSTR);
                BSTR_S.init(false, IB, P);
                I.init(false, IB, ISTR);
                ISTR.init(true, IB, ISTR);
                IB.init(true, P, IBSTR);
                IBSTR.init(true, I, IBSTR);
            }

            /**
             * State represents bold.
             */
            private boolean bold;

            /**
             * State represents italic.
             */
            private boolean italic;

            /**
             * On star transition flushes context to generate a block.
             */
            private boolean starFlush;

            /**
             * State to transition to on star.
             */
            private State starState;

            /**
             * State to transition to on character.
             */
            private State charState;

            /**
             * Construct with formatting flags.
             * 
             * @param bold
             *               {@code true} if flushes with bold.
             * @param italic
             *               {@code true} if flushes with italic.
             */
            private State(boolean bold, boolean italic) {
                this.bold = bold;
                this.italic = italic;
            }

            /**
             * Initialize the state tranistions.
             * 
             * @param starFlush
             *                  if to flush on a star.
             * @param starState
             *                  transition to on a star.
             * @param charState
             *                  tranistion to on a character.
             */
            private void init(boolean starFlush, State starState, State charState) {
                this.starFlush = starFlush;
                this.starState = starState;
                this.charState = charState;
            }

            /**
             * Process the given character and transition the state.
             * 
             * @param ctx
             *            the operating context.
             * @param ch
             *            the character to process.
             * @return the resultant state.
             */
            public State transition(Scanner ctx, char ch) {
                if (ch == '*') {
                    if (!starFlush) {
                        //System.out.println (ch + " -> " + starState.name());
                        return starState;
                    }
                    //System.out.println (ch + " -> " + starState.name() + "  (\"" + ctx.str + "\"" + (bold ? "b" : "") + (italic ? "i" : "") + ")");
                    ctx.flush(bold, italic);
                    return starState;
                }
                //System.out.println (ch + " -> " + charState.name());
                ctx.add(ch);
                return charState;
            }

            /**
             * Terminal transition from the given state (flushes).
             * 
             * @param ctx
             *            the operating context.
             */
            public void end(Scanner ctx) {
                ctx.flush(bold, italic);
            }
        }
    }
}
