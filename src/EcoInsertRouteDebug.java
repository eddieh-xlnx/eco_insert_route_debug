/*
 * Copyright (C) 2023, Advanced Micro Devices, Inc.  All rights reserved.
 *
 * Author: Eddie Hung, AMD
 *
 * SPDX-License-Identifier: MIT
 *
 */

import com.xilinx.rapidwright.design.Design;
import com.xilinx.rapidwright.design.Module;
import com.xilinx.rapidwright.design.ModuleInst;
import com.xilinx.rapidwright.device.Device;
import com.xilinx.rapidwright.eco.ECOTools;
import com.xilinx.rapidwright.edif.EDIFNetlist;
import com.xilinx.rapidwright.rwroute.PartialRouter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

class EcoInsertRouteDebug {
    public static void main(String[] args) {
        Design baseDesign = Design.readCheckpoint("files/boom_medium_routed.dcp");
        Design debugDesign = Design.readCheckpoint("files/fifo36_routed.dcp");

        boolean unrouteStaticNets = false;
        Module debugModule = new Module(debugDesign, unrouteStaticNets);

        ModuleInst debug1ModuleInst = baseDesign.createModuleInst("debug1", debugModule);
        debug1ModuleInst.placeOnOriginalAnchor();

        // List<ModuleInst> debugInsts = new ArrayList();
        // debugInsts.add(debug1ModuleInst);

        // String clkName = "clock_uncore_clock_IBUF_BUFG";
        // List<String> netNames = new ArrayList();
        // for (int i = 0; i < 36; i++) {
        //     netNames.add("system/tile_prci_domain/tile_reset_domain_tile/core/csr/s1_pc_reg[" + i + "]");
        // }
        // EDIFNetlist baseNetlist = baseDesign.getNetlist();
        // List<String> netPinList = buildNetPinList(baseNetlist, clkName, netNames, debugInsts);
        // ECOTools.connectNet(baseDesign, netPinList);

        // PartialRouter.routeDesignPartialNonTimingDriven(baseDesign, null);

        baseDesign.writeCheckpoint("boom_medium_debug.dcp");
    }

    public static List<String> buildNetPinList(EDIFNetlist baseNetlist,
                                               String clkName,
                                               List<String> netNames,
                                               List<ModuleInst> debugInsts) {
        List<String> netPinList = new ArrayList<>();
        for (ModuleInst debugInst : debugInsts) {
            netPinList.add(clkName + " " + debugInst.getName() + "/WRCLK");
        }

        final int numPinsPerDebugInst = 36;
        int numPins = debugInsts.size() * numPinsPerDebugInst;
        for (int i = 0; i < numPins; i++) {
            String netName = i < netNames.size() ? netNames.get(i) : null;
            if (netName == null || baseNetlist.getHierNetFromName(netName) == null) {
                netName = "<const1>";
            }
            String debugInstName = debugInsts.get(i / numPinsPerDebugInst).getName();
            String pinName = debugInstName + "/DIN[" + (i % numPinsPerDebugInst) + "]";
            netPinList.add(netName + " " + pinName);

            System.out.println("INFO: Connecting net '" + netName + "' to pin '" + pinName + "'");
        }

        return netPinList;
    }
}

