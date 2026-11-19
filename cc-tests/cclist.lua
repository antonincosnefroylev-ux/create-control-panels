-- cclist: every console this computer reaches, and every component on each.
-- Usage: cclist
-- Checks: joined consoles all show up, the touched one is marked, names and
-- positions are right, and each part's index / linked / frequency is right.

local console = peripheral.find("control_console")
if not console then error("No control_console found: put the computer against a console", 0) end

local function short(id) return id and id:gsub("^minecraft:", "") or "-" end

local lines = {}
local consoles = console.getConsoles()
table.insert(lines, #consoles .. " console(s) reachable:")
for i, c in ipairs(consoles) do
  table.insert(lines, ("%d. %s @ %d,%d,%d  %d parts%s"):format(
    i, c.name or "(unnamed)", c.x, c.y, c.z, c.components, c.attached and "  <- touched" or ""))
  for _, p in ipairs(console.getComponents(c)) do
    local freq = p.linked and (short(p.frequency.first) .. " + " .. short(p.frequency.second)) or "unlinked"
    table.insert(lines, ("  #%d %-13s %-2s %2d  %s"):format(
      p.index, p.type, p.direction == "transmit" and "TX" or "RX", p.value, freq))
  end
end
textutils.pagedPrint(table.concat(lines, "\n"))
