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
package com.effacy.jui.ui.client.control.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.effacy.jui.core.client.component.IComponent;
import com.effacy.jui.core.client.component.IShowHideListener;
import com.effacy.jui.core.client.control.IControl;
import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.control.IModifiedListener;
import com.effacy.jui.platform.util.client.Logger;
import com.effacy.jui.platform.util.client.StringSupport;
import com.effacy.jui.ui.client.control.builder.GroupBuilder.RowBuilder.RowCell;
import com.effacy.jui.ui.client.control.builder.IGroupBuilder.IModification;
import com.effacy.jui.ui.client.control.builder.IGroupBuilder.IRowBuilder.IControlCell;

public class ModificationContext implements IModificationContext {

    private Map<String, IControl<?>> controlByReference = new HashMap<>();
    private Map<String, IComponent> componentByReference = new HashMap<>();
    private Map<String, GroupBuilder<?,?>> groupsByReference = new HashMap<>();
    private Map<String, List<GroupBuilder<?,?>>> conditionalGroupsByGroup = new HashMap<>();

    public <SRC,DST> void process(GroupBuilder<SRC,DST> grp) {
        if (!StringSupport.empty (grp.getReference ()))
            groupsByReference.put (grp.getReference (), grp);
        if (!StringSupport.empty (grp.getConditionalGroup ())) {
            List<GroupBuilder<?,?>> builders = conditionalGroupsByGroup.get (grp.getConditionalGroup ());
            if (builders == null) {
                builders = new ArrayList<> ();
                conditionalGroupsByGroup.put (grp.getConditionalGroup (), builders);
            }
            builders.add (grp);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void process(IControlCell icell) {
        RowCell cell = (RowCell) icell;
        if (cell.control () != null) {
            if (cell.modification != null) {
                cell.control ().addListener ((IModifiedListener) (ctl -> {
                    ((IModification<Object,IControl<Object>>) (Object) cell.modification)
                        .modified (ModificationContext.this, (IControl<Object>) (Object) ctl);
                }));
            }
            if (!StringSupport.empty (cell.reference)) {
                controlByReference.put (cell.reference, cell.control);
                componentByReference.put (cell.reference, cell.control);
            }
            if (cell.disabled)
                cell.control ().disable ();
            if (cell.hidden)
                cell.control ().hide();
            cell.control ().addListener (IShowHideListener.create ((cpt, show) -> {
                // TODO: Activate.
            }));
        } else if (cell.component () != null) {
            if (!StringSupport.empty (cell.reference))
                componentByReference.put (cell.reference, cell.component ());
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <W> IControl<W> control(String reference) {
        if (reference == null)
            return null;
        return (IControl<W>) controlByReference.get(reference);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <W> W value(String reference) {
        IControl<W> ctl = control (reference);
        if (ctl != null)
            return ctl.value ();

        // Here we try to find a corresponding group by reference.
        GroupBuilder<?,?> contributor = groupsByReference.get (reference);
        if (contributor != null)
            return (W) (Object) contributor.isOpen ();

        // Finally we try to find a group collection.
        List<GroupBuilder<?,?>> groups = conditionalGroupsByGroup.get (reference);
        if (groups != null) {
            for (GroupBuilder<?,?> group : groups) {
                if (group.isOpen ())
                    return (W) (Object) group.getConditionalDescriminator ();
            }
        }

        // Default is to return null.
        return null;
    }

    @Override
    public <W> void set(String reference, Value<W> value) {
        IControl<W> ctl = control(reference);
        if (ctl != null) {
            if (value == null)
                value = Value.of (null);
            ctl.setValue (value);
        } else {
            if ((value != null) && (value.value() != null)) {
                if (_setConditionalGroup(reference, value.value ().toString ()))
                    return;
            }
            Logger.error ("Unable to find control referenced by \"" + reference + "\" to assign value to");
        }
    }

    protected boolean _setConditionalGroup(String reference, String value) {
        List<GroupBuilder<?,?>> groups = conditionalGroupsByGroup.get (reference);
        if (groups == null)
            return false;
        for (GroupBuilder<?,?> group : groups) {
            if (value.equals (group.getConditionalDescriminator())) {
                group._activate ();
                return true;
            }
        }
        return false;
    }

    @Override
    public void enable(String... references) {
        for (String reference : references) {
            IComponent cpt = componentByReference.get (reference);
            if (cpt != null) {
                cpt.enable ();
            } else {
                GroupBuilder<?,?> group = groupsByReference.get (reference);
                if (group != null)
                    group._enable ();
            }
        }
    }

    @Override
    public void disable(String... references) {
        for (String reference : references) {
            IComponent cpt = componentByReference.get(reference);
            if (cpt != null) {
                cpt.disable ();
            } else {
                GroupBuilder<?,?> group = groupsByReference.get (reference);
                if (group != null)
                    group._disable ();
            }
        }
    }

    @Override
    public void show(String... references) {
        for (String reference : references) {
            IComponent cpt = componentByReference.get(reference);
            if (cpt != null) {
                cpt.show();
            } else {
                GroupBuilder<?,?> group = groupsByReference.get (reference);
                if (group != null)
                    group._show ();
            }
        }
    }

    @Override
    public void hide(String... references) {
        for (String reference : references) {
            IComponent cpt = componentByReference.get(reference);
            if (cpt != null) {
                cpt.hide();
            } else {
                GroupBuilder<?,?> group = groupsByReference.get (reference);
                if (group != null)
                    group._hide ();
            }
        }
    }

    @Override
    public boolean groupOpen(String reference) {
        GroupBuilder<?,?> contributor = groupsByReference.get (reference);
        return (contributor != null) ? contributor.isOpen () : false;
    }
}
