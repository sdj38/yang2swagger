/*
 * Copyright (c) 2016 MRV Communications, Inc. All rights reserved.
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Christopher Murch <cmurch@mrv.com>
 *      Bartosz Michalik <bartosz.michalik@amartus.com>
 */

package com.mrv.yangtools.codegen.impl;

import io.swagger.models.properties.*;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.*;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

/**
 * Supports type conversion between YANG and swagger
 * @author cmurch@mrv.com
 * @author bartosz.michalik@amartus.com
 */
public class TypeConverter {

    private SchemaContext ctx;

    public TypeConverter(SchemaContext ctx) {
        this.ctx = ctx;
    }

    private static final Logger log = LoggerFactory.getLogger(TypeConverter.class);

    /**
     * Convert YANG type to swagger property
     * @param type YANG
     * @param parent for scope computation (to support leafrefs
     * @return property
     */
    @SuppressWarnings("ConstantConditions")
    public Property convert(TypeDefinition<?> type, SchemaNode parent) {
        TypeDefinition<?> baseType = type.getBaseType();


        if(type instanceof LeafrefTypeDefinition) {
            log.debug("leaf node {}",  type);
            baseType = SchemaContextUtil.getBaseTypeForLeafRef((LeafrefTypeDefinition) type, ctx, parent);
        }

        if(baseType instanceof BooleanTypeDefinition) {
            return new BooleanProperty();
        }

        if(baseType instanceof IntegerTypeDefinition || baseType instanceof UnsignedIntegerTypeDefinition) {
            //TODO [bmi] how to map int8 type ???
            BaseIntegerProperty integer = new IntegerProperty();
            if (BaseTypes.isInt64(baseType) || BaseTypes.isUint32(baseType)) {
                integer = new LongProperty();
            }
            return integer;
        }

        StringProperty result = new StringProperty();

        if(type instanceof EnumTypeDefinition) {
            result.setEnum(((EnumTypeDefinition) type).getValues()
                    .stream()
                    .map(EnumTypeDefinition.EnumPair::getName).collect(Collectors.toList()));
        } else if(baseType instanceof EnumTypeDefinition) {
            result.setEnum(((EnumTypeDefinition) baseType).getValues()
                    .stream()
                    .map(EnumTypeDefinition.EnumPair::getName).collect(Collectors.toList()));
        }

        return result;
    }
}
