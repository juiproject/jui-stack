package com.effacy.jui.ui.client.fragments;

import org.junit.jupiter.api.Test;

import com.effacy.jui.core.client.test.AbstractJUITest;

public class BtnTest extends AbstractJUITest {

    @Test
    public void button_with_icon_and_label() {
        build(Btn.$("Save").icon("fa-save"))
            .print()
            .htmlEquals("""
                <div>
                    <button class="fragment">
                        <em class="fa-save"></em>
                        <span class="runningpart">
                            <em class="fas fa-spin fa-spinner"></em>
                        </span>
                        <span class="label">Save</span>
                    </button>
                </div>
                """)
            .child("button.fragment", c1 -> {
                c1.exists().tagEquals("button").child("em.fa-save", c2 -> {
                    c2.exists().tagEquals("em");
                });
                c1.child("span.runningpart", c2 -> {
                    c2.exists().tagEquals("span").child("em.fa-spinner", c3 -> {
                        c3.exists().tagEquals("em").hasClass("fa-spin");
                    });
                });
            })
            .child("button.fragment", c1 -> {
                c1.child("span.label", c2 -> {
                    c2.exists().tagEquals("span").textEquals("Save");
                });
            });
    }
}
