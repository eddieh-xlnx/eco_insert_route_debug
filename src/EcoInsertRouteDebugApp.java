/*
 * Copyright (C) 2023, Advanced Micro Devices, Inc.  All rights reserved.
 *
 * Author: Eddie Hung, AMD
 *
 * SPDX-License-Identifier: MIT
 *
 */

import com.xilinx.rapidwright.debug.ILAInserter;
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

class EcoInsertRouteDebugApp {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("USAGE: <input.dcp> <output.dcp>");
            System.exit(1);
        }
        Design baseDesign = Design.readCheckpoint(args[0]);
        Design debugDesign = Design.readCheckpoint("files/fifo36_routed.dcp");

        boolean unrouteStaticNets = false;
        Module debugModule = new Module(debugDesign, unrouteStaticNets);

        ModuleInst debug1ModuleInst = baseDesign.createModuleInst("debug1", debugModule);
        Device device = baseDesign.getDevice();
        debug1ModuleInst.place(device.getSite("RAMB36_X7Y34"));

        ModuleInst debug2ModuleInst = baseDesign.createModuleInst("debug2", debugModule);
        debug2ModuleInst.place(device.getSite("RAMB36_X4Y41"));

        List<ModuleInst> debugInsts = new ArrayList();
        debugInsts.add(debug1ModuleInst);
        debugInsts.add(debug2ModuleInst);

        String clkName = "clock_uncore_clock_IBUF_BUFG";
        List<String> netNames = ILAInserter.getNetsMarkedForDebug(baseDesign);

        EDIFNetlist baseNetlist = baseDesign.getNetlist();
        List<String> netPinList = buildNetPinList(baseNetlist, clkName, netNames, debugInsts);
        ECOTools.connectNet(baseDesign, netPinList);

        PartialRouter.routeDesignPartialNonTimingDriven(baseDesign, null);

        baseDesign.writeCheckpoint(args[1]);
    }

    public static List<String> buildNetPinList(EDIFNetlist netlist,
                                               String clkName,
                                               List<String> netNames,
                                               List<ModuleInst> debugInsts) {
        return EcoInsertRouteDebug.buildNetPinList(netlist, clkName, netNames, debugInsts);
    }
}
