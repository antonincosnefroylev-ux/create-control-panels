-- ccunlinked: issue #1, working components that have NO frequency.
-- Usage: ccunlinked [console name]   (default: the console the computer touches)
--
-- 1. Sweeps every unlinked lamp/meter from 0 to 15 and back to 8.
-- 2. Then mirrors: flip or turn any unlinked control on that console and
--    every unlinked lamp/meter follows its value. Ctrl+T stops.
-- Afterwards, shut the computer down (hold Ctrl+S): the lamps must go dark.

local console = peripheral.find("control_console")
if not console then error("No control_console found: put the computer against a console", 0) end
local target = arg[1]

local controls, displays = {}, {}
for _, p in ipairs(console.getComponents(target)) do
  if not p.linked then
    table.insert(p.direction == "transmit" and controls or displays, p)
  end
end
print(("Unlinked: %d control(s), %d lamp/meter(s)"):format(#controls, #displays))
if #displays == 0 then error("Add a lamp or meter with no frequency to this console", 0) end

local function showAll(value)
  for _, p in ipairs(displays) do console.setValue(p.index, value, target) end
end

print("Sweeping 0..15 (no Redstone Link anywhere should react)")
for v = 0, 15 do showAll(v); sleep(0.15) end
showAll(8)
for _, p in ipairs(displays) do
  print(("  #%d %s reads back %d (expect 8)"):format(p.index, p.type, console.getValue(p.index, target)))
end

if #controls == 0 then print("No unlinked controls to mirror; done.") return end

-- which console the events must come from
local me
for _, c in ipairs(console.getConsoles()) do
  if (target == nil and c.attached) or (target ~= nil and c.name == target) then me = c end
end
local isControl = {}
for _, p in ipairs(controls) do isControl[p.index] = true end

print("Mirroring: work an unlinked control. Ctrl+T to stop.")
while true do
  local _, _, index, value, source = os.pullEvent("console_signal")
  if source.x == me.x and source.y == me.y and source.z == me.z and isControl[index] then
    print(("  control #%d -> %d"):format(index, value))
    showAll(value)
  end
end
