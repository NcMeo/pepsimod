/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2017-2018 DaPorkchop_
 *
 * Permission is hereby granted to any persons and/or organizations using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or any derivatives of the work for commercial use or any other means to generate income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing and/or trademarking this software without explicit permission from DaPorkchop_.
 *
 * Any persons and/or organizations using this software must disclose their source code and have it publicly available, include this license, provide sufficient credit to the original author of the project (IE: DaPorkchop_), as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package net.daporkchop.pepsimod.command.impl;

import net.daporkchop.pepsimod.command.api.Command;
import net.daporkchop.pepsimod.util.PepsiUtils;
import net.daporkchop.pepsimod.util.config.impl.WaypointsTranslator;
import net.daporkchop.pepsimod.util.misc.waypoints.Waypoint;
import net.daporkchop.pepsimod.util.misc.waypoints.pathfind.PathFindTickListener;
import net.daporkchop.pepsimod.util.misc.waypoints.pathfind.PathFinder;
import net.daporkchop.pepsimod.util.misc.waypoints.pathfind.PathPos;
import net.daporkchop.pepsimod.util.misc.waypoints.pathfind.PathProcessor;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class GoToCommand extends Command {
    public static GoToCommand INSTANCE;
    public final ArrayList<PathPos> path = new ArrayList<>();
    public PathFinder pathFinder;
    public PathProcessor processor;
    public boolean enabled;
    public BlockPos endGoal;

    public GoToCommand() {
        super("goto");
        INSTANCE = this;
    }

    @Override
    public void execute(String cmd, String[] args) {
        if (PathFindTickListener.INSTANCE == null) {
            new PathFindTickListener();
        }
        if (enabled) {
            PathFindTickListener.INSTANCE.disable();
            clientMessage("Disabled pathfinder.");
            return;
        }
        if (args.length == 2) {
            Waypoint waypoint = WaypointsTranslator.INSTANCE.getWaypoint(args[1]);
            if (waypoint == null) {
                clientMessage("No such waypoint: " + args[1]);
                return;
            } else {
                setGoal(new BlockPos(waypoint.x, waypoint.y, waypoint.z));
            }
        } else if (args.length == 4) {
            int x, y, z;
            try {
                x = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                clientMessage("Invalid integer: " + args[1]);
                return;
            }
            try {
                y = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                clientMessage("Invalid integer: " + args[2]);
                return;
            }
            try {
                z = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                clientMessage("Invalid integer: " + args[3]);
                return;
            }
            setGoal(new BlockPos(x, y, z));
        } else {
            clientMessage("Usage: \u00A7o.goto <waypoint>\u00A7r or \u00A7o.goto <x> <y> <z>");
            return;
        }
        enabled = true;
        PepsiUtils.tickListeners.add(PathFindTickListener.INSTANCE);
        PepsiUtils.wurstRenderListeners.add(PathFindTickListener.INSTANCE);
        clientMessage("Going to position: " + endGoal.getX() + ", " + endGoal.getY() + ", " + endGoal.getZ());
        clientMessage("Run \u00A7o.goto\u00A7r to stop.");
        clientMessage("\u00A7cWARNING! The pathfinder is currently VERY experimental.");
        clientMessage("\u00A7cUse at your own risk.");
    }

    public void setGoal(BlockPos goal) {
        if (endGoal == null) {
            endGoal = goal;
            pathFinder = new PathFinder(goal);
        } else {
            throw new IllegalStateException("Attempted to start pathfinder while endGoal was set");
        }
    }

    public boolean hasReachedFinalGoal() {
        if (endGoal == null) {
            return true;
        }
        return mc.player.getDistanceSq(endGoal) <= 64;
    }

    @Override
    public String getSuggestion(String cmd, String[] args) {
        return ".goto";
    }
}
