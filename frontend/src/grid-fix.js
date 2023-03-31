//node_modules/@vaadin/flow-frontend/gridProConnector.js
(function () {
    const handlers = [
        {
            editorTag: 'amount-in-units-field',
            setValuesOnEditor: function (values, component) {

                // Function for obtaining a unit object from the [amount-in-units-field] component
                // given a unit label
                function getUnitDetails(unitLabel, component) {
                    if (component.units.length == 1) {
                        return component.units[0];
                    }
                    for (let i = 0; i < component.units.length; i++) {
                        // Find the actual unit from its label
                        if (component.units[i].label == unitLabel) {
                            return component.units[i];
                        }
                    }
                    // Couldn't find a matching unit - just return 
                    // some sensible defaults for formatting
                    return {
                        numFractionDigits: 0,
                        separator: ',',
                        label: unitLabel,
                        value: unitLabel
                    }
                }

                let displayedUnit = component._currentUnit ? component._currentUnit.label: '';
                let displayedAmount;
                if (values.length == 1) {
                    displayedAmount = values[0];
                } else {
                    displayedUnit = values[0];
                    displayedAmount = values[1];
                }

                // Get unit details from the editor component itself
                let unitDetails = getUnitDetails(displayedUnit, component);
                let amount = displayedAmount;
                if (displayedAmount) {
                    // Remove any numeric formatting and ensure correct decimal places
                    amount = Number(displayedAmount.replaceAll(unitDetails.separator, '')).toFixed(unitDetails.numFractionDigits);
                }
                // Set the current value and units on the [amount-in-units-field] component
                component.currentValue = {
                    unit: unitDetails.value,
                    value: amount
                };
            },
            valuesForReadOnly: function (editorComponent) {
                const values = [];
                if (!editorComponent.hideUnits) {
                    const formattedUnit = editorComponent._currentUnit ? editorComponent._currentUnit.label : '';
                    values.push(formattedUnit);
                }
                const formattedAmount = editorComponent.shadowRoot.querySelector('input').value;
                values.push(formattedAmount);
                return values;
            }
        }, {
            editorTag: 'vaadin-text-field',
            setValuesOnEditor: function (values, component) {
                component.value = values[0];
            },
            valuesForReadOnly: function (editorComponent) {
                return [editorComponent.value];
            }
        }, {
            editorTag: 'vaadin-select',
            setValuesOnEditor: function (values, component) {
                component.value = values[0];
            },
            valuesForReadOnly: function (editorComponent) {
                return [editorComponent.shadowRoot.querySelector('vaadin-item').innerText];
            }
        }
    ];

    const tryCatchWrapper = function (callback) {
        return window.Vaadin.Flow.tryCatchWrapper(callback, 'Vaadin Grid Pro', 'vaadin-grid-pro-flow');
    };

    function isEditedRow(grid, rowData) {
        return grid.__edited && grid.__edited.model.item.key === rowData.item.key;
    }
    
    function isThereAProblem(){
		try { 
			var a = {}; 
			a.debug(); 
		} catch(ex) {
			let stack = ex.stack || '';
			let hasIssue = stack.includes("_startEdit");
			console.log(hasIssue);
			return hasIssue;
		}
		return false;
	}

    function getHandler(column, component) {
        if (column.editorType === 'custom') {
            for (let i = 0; i < handlers.length; i++) {
                if (component.localName === handlers[i].editorTag) {
                    return handlers[i];
                }
            }
        }
        return null;
    }

    window.Vaadin.Flow.gridProConnector = {
        setEditModeRenderer: (column, component) => tryCatchWrapper(function (column, component) {
            column.editModeRenderer = tryCatchWrapper(function editModeRenderer(root, _, rowData) {
				isThereAProblem();
                if (!isEditedRow(this._grid, rowData)) {
					console.log("editModeRenderer- !is edited row");
                    this._grid._stopEdit();
                    return;
                }

                if (component.parentNode === root) {
					console.log("parentNode");
                    return;
                }

                let handler = getHandler(column, component);
                //change
                if (handler) {
                    // Get the [tr] being edited
                    let cell = root.assignedSlot.parentElement

                    let numParams = cell.__savedTemplate._templateInfo.propertyEffects.item.length;
                    let paramValues = [];
                    for (let i = 0; i < numParams; i++) {
                        let paramName = cell.__savedTemplate._templateInfo.propertyEffects.item[i].trigger.name;
                        let paramValue = this.get(paramName, rowData);
                        paramValues.push(paramValue);
                    }
                    handler.setValuesOnEditor(paramValues, component);
                }
                // Continue with normal grid processing
					console.log("append child");
                root.appendChild(component);
					console.log("_cancelStopEdit");
                this._grid._cancelStopEdit();
					console.log("focus");
                component.focus();
            });
            
            column._setEditorValue = function (editor, value) { };

            let handler = getHandler(column, component);
            if (!handler) {
                column._getEditorValue = function (editor, value) { };
            } else {
                const originalFunction = column._removeEditor;
                column._removeEditor = function (cell, rowData) {
                    let templateParams = cell.__savedTemplate._templateInfo.propertyEffects.item;
                    let values = handler.valuesForReadOnly(this._getEditorComponent(cell));
                    if (values.length > 0) {
                        // Set actual values for those keys in the row data
                        for (let i = 0; i < values.length; i++) {
                            let paramName = templateParams[i].trigger.name;
                            this.set(paramName, values[i], rowData);
                        }
                        // Find the [tr] element for the cell being edited
                        const editedKey = column._gridValue.__edited.model.item.key;
                        const rows = column._gridValue.__data.scrollTarget.rows;
                        const editedRow = [...rows].find(tr => tr._item && tr._item.key === editedKey) || {};

                        if (editedRow._item) {
                            // Update the rendered item data here with the values
                            for (let i = 0; i < values.length; i++) {
                                let paramName = templateParams[i].trigger.name;
                                editedRow._item[paramName.replace('item.', '')] = values[i];
                            }
                        }
                    }
                    // Continue with normal grid processing
                    return originalFunction.apply(column, arguments);
                };
            }
        })(column, component),

        patchEditModeRenderer: column => tryCatchWrapper(function (column) {
            column.__editModeRenderer = tryCatchWrapper(function __editModeRenderer(root, column, rowData) {
				console.log("patch edit mode renderer");
                const cell = root.assignedSlot.parentNode;
                const grid = column._grid;

                if (!isEditedRow(grid, rowData)) {
					console.log("patch edit mode renderer - isEditedRow");
                    grid._stopEdit();
                    return;
                }

                const tagName = column._getEditorTagName(cell);
                if (!root.firstElementChild || root.firstElementChild.localName.toLowerCase() !== tagName) {
                    root.innerHTML = `<${tagName}></${tagName}>`;
                }
            });
        })(column)
    };
})();