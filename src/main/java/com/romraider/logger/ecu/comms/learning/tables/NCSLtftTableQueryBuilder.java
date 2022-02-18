/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.logger.ecu.comms.learning.tables;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryData;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.definition.EcuAddress;
import com.romraider.logger.ecu.definition.EcuAddressImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.EcuParameterImpl;
import com.romraider.logger.ecu.ui.paramlist.ParameterRow;
import com.romraider.util.HexUtil;

/**
 * Build an EcuQuery for each of the cells in the FLKC RAM table.
 */
public class NCSLtftTableQueryBuilder {
    private static final Logger LOGGER =
            Logger.getLogger(NCSLtftTableQueryBuilder.class);

    public NCSLtftTableQueryBuilder() {
    }

    /**
     * Build an EcuQuery for each cell of the LTFT RAM table. <i>Note this
     * returns an extra null query for column 0 of each row which is later
     * populated with the row header (RPM Ranges) data.</i>
     * @param ltft - a ParameterRow item that helps to identify the
     * ECU bitness and provide a Converter for the raw data.
     * @param ltftAddr - the address in RAM of the start of the table.
     * @param rows - the number of rows in the table.
     * @param columns - the number of columns in the table.
     * @return EcuQueries divided into groups to query each row separately to
     * avoid maxing out the ECU send/receive buffer.
     */
    public final List<List<EcuQuery>> build(
            ParameterRow ltft,
            int ltftAddr,
            int rows,
            int columns) {

        final List<List<EcuQuery>> ltftQueryRows = new ArrayList<List<EcuQuery>>();
        final EcuData parameter = (EcuData) ltft.getLoggerData();
        int dataSize = EcuQueryData.getDataLength(parameter);
        if (LOGGER.isDebugEnabled())
            LOGGER.debug(
                String.format(
                        "LTFT Data format rows:%d col:%d " +
                        "dataSize:%d LTFT:%s",
                        rows, columns, dataSize,
                        ltft.getLoggerData().getId()));

        int i = 0;
        for (int j = 0; j < rows; j++) {
            final List<EcuQuery> ltftQueryCols = new ArrayList<EcuQuery>();
            ltftQueryCols.add(null);
            for (int k = 0; k < columns; k++) {
                String id = "flkc-r" + j + "c" + k;
                final String addrStr =
                        HexUtil.intToHexString(
                                ltftAddr + (i * dataSize));
                if (LOGGER.isDebugEnabled())
                    LOGGER.debug(
                        String.format(
                                "LTFT Data row:%d col:%d addr:%s",
                                j, k, addrStr));
                final EcuAddress ea = new EcuAddressImpl(addrStr, dataSize, -1);
                final EcuParameterImpl epi =
                    new EcuParameterImpl(id, addrStr, id, ea, null, null, null,
                        new EcuDataConvertor[] {
                            ltft.getLoggerData().getSelectedConvertor()
                        }
                    );
                ltftQueryCols.add(new EcuQueryImpl(epi));
                i++;
            }
            ltftQueryRows.add(ltftQueryCols);
        }
        return ltftQueryRows;
    }
}
