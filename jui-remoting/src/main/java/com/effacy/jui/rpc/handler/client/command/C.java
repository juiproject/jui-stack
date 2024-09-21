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
package com.effacy.jui.rpc.handler.client.command;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.rpc.handler.client.query.Result;
import com.effacy.jui.rpc.handler.client.ref.Ref;
import com.effacy.jui.rpc.handler.client.ref.UniqueRef;

/**
 * Base class for commands that include a reference lookup, general lookup and
 * construction specifications.
 *
 * @author Jeremy Buckley
 */
public abstract class C extends E implements ICommand {

    /**
     * A unique ID for the associated object.
     */
    private UniqueRef _reference = new UniqueRef ();

    /**
     * Label to use to reference violations back to this command.
     */
    private String _label;

    /**
     * The lookup mode (if any).
     */
    private Object _lookup;

    /**
     * The constructor (if any).
     */
    private Construct _construct;

    /**
     * Collection of actions to perform.
     */
    private List<IAction> _actions;

    /**
     * If to delete the entity.
     */
    private boolean delete = false;

    /**
     * Construct with no lookup ID (creator).
     */
    protected C() {
        // Nothing.
    }


    /**
     * Construct with a lookup. The lookup will be used by the corresponding
     * resolver to lookup an existing entity or object that is referenced by the
     * lookup and for which this command performs actions against.
     * <p>
     * In some cases the underlying object will have been created as part of the
     * overall command execution sequence (and thus a unique reference will not
     * be known at the time of command construction). In this case one may pass
     * the associated construct commands reference lookup (this is obtained by
     * calling {@link #reference()} and passing that as the lookup. The command
     * resolver will attempt to lookup any associated object that has previously
     * been resolved in the command execution sequence.
     * 
     * @param lookup
     *            the lookup.
     */
    protected C(Ref lookup) {
        this._lookup = lookup;
    }


    /**
     * Construct with a constructor.
     * 
     * @param construct
     *            the construct to use.
     */
    protected C(Construct construct) {
        this._construct = construct;
    }


    /**
     * Construct with a command to extract a reference from.
     * 
     * @param cmd
     *            the command.
     */
    protected C(ICommand cmd) {
        this._lookup = cmd.reference ();
    }


    /**
     * Marks the command as one to delete.
     * 
     * @return This instance.
     */
    public C delete() {
        setDelete (true);
        return this;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.client.command.ICommand#reference()
     */
    @Override
    public UniqueRef reference() {
        if ((_lookup != null) && (_lookup instanceof UniqueRef))
            return (UniqueRef) _lookup;
        return _reference;
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.effacy.jui.rpc.handler.client.command.ICommand#lookup()
     */
    @Override
    public Object lookup() {
        return _lookup;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.com.effacy.jui.rpc.handler.client.command.dto.ICommand#label()
     */
    @Override
    public String label() {
        if (_label == null)
            _label = reference ().toString ();
        return _label;
    }


    /**
     * Assigns a label to the command.
     * 
     * @param label
     *            the label to assign.
     * @return this instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends C> T label(String label) {
        set_label (label);
        return (T) this;
    }


    /**
     * Adds an action.
     * 
     * @param action
     *            the action to add.
     * @return the added action.
     */
    public <A extends IAction> A add(A action) {
        if (action != null)
            get_actions ().add (action);
        return action;
    }


    /*************************************************************************
     * Getters and setters.
     *************************************************************************/

    /**
     * Obtains the delete marker.
     * 
     * @return The delete marker.
     */
    public boolean isDelete() {
        return delete;
    }


    /**
     * Sets the delete marker (property).
     * 
     * @param delete
     *            the delete marker value.
     */
    public void setDelete(boolean delete) {
        this.delete = delete;
    }


    /**
     * Special getter for the reference.
     * 
     * @return the reference.
     */
    public UniqueRef get_reference() {
        return _reference;
    }


    /**
     * Special setter for the reference.
     * 
     * @param reference
     *            the reference.
     */
    public void set_reference(UniqueRef reference) {
        this._reference = reference;
    }


    /**
     * Special getter for the lookup.
     * 
     * @return the lookup.
     */
    public Object get_lookup() {
        return _lookup;
    }


    /**
     * Special setter for the lookup.
     * 
     * @param lookup
     *            the lookup.
     */
    public void set_lookup(Ref lookup) {
        this._lookup = lookup;
    }


    /**
     * Special getter for the construct.
     * 
     * @return the construct.
     */
    public Construct get_construct() {
        return _construct;
    }


    /**
     * Special setter for the construct.
     * 
     * @param _construct
     *            the construct.
     */
    public void set_construct(Construct _construct) {
        this._construct = _construct;
    }


    /**
     * Special getter for the label.
     * 
     * @return the label.
     */
    public String get_label() {
        return _label;
    }


    /**
     * Special setter for assigning a label.
     * 
     * @param _label
     *            the label.
     */
    public void set_label(String _label) {
        this._label = _label;
    }


    /**
     * Serialisation only.
     */
    public List<IAction> get_actions() {
        if (_actions == null)
            _actions = new ArrayList<IAction> ();
        return _actions;
    }


    /**
     * Serialisation only.
     */
    public void set_actions(List<IAction> _actions) {
        this._actions = _actions;
    }


    /**
     * Determines if the action is a lookup.
     * 
     * @return {@code true} if it is.
     */
    public boolean actionLookup() {
        return (_lookup != null);
    }


    /**
     * Determines if the action is a construction.
     * 
     * @return {@code true} if it is.
     */
    public boolean actionConstruct() {
        return (_construct != null);
    }


    /**
     * {@inheritDoc}
     *
     * @see com.effacy.jui.rpc.handler.client.command.command.dto.E#dirty()
     */
    @Override
    public boolean dirty() {
        return super.dirty () || !get_actions ().isEmpty ();
    }


    /**
     * {@inheritDoc}
     * 
     * @see com.com.effacy.jui.rpc.handler.client.command.dto.E#toStringPreamble(java.lang.StringBuffer)
     */
    @Override
    protected boolean toStringPreamble(StringBuffer sb) {
        boolean comma = false;
        if (_lookup != null) {
            comma = true;
            sb.append ("_lookup{");
            sb.append (_lookup);
            sb.append ('}');
        }
        if (delete) {
            if (comma)
                sb.append (',');
            sb.append ("delete");
            comma = true;
        }
        return comma;
    }

    /*************************************************************************
     * Useful classes.
     *************************************************************************/

    /**
     * Serialisable implementation of {@link IAction}.
     */
    public static abstract class Action extends Result implements IAction {}
}
