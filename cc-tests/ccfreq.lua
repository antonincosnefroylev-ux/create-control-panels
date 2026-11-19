-- ccfreq: sets a component's Redstone Link frequency from the computer.
-- Usage: ccfreq <#> <first item|-> <second item|-> [console name]
--   "-" leaves a slot blank; "- -" unlinks the part. "minecraft:" may be left off.
--   Example: ccfreq 3 iron_ingot redstone
--            ccfreq 2 - lever Reactor
-- Checks: the designer shows the new items, and a Create Redstone Link set to
-- the same pair now talks to the part.

local console = peripheral.find("control_console")
if not console then error("No control_console found: put the computer against a console", 0) end

local index = tonumber(arg[1])
if not index or not arg[2] or not arg[3] then
  error("Usage: ccfreq <#> <first|-> <second|-> [console name]", 0)
end
local function item(s)
  if s == "-" then return nil end
  if not s:find(":") then return "minecraft:" .. s end
  return s
end
local target = arg[4]

console.setFrequency(index, item(arg[2]), item(arg[3]), target)
local p = console.getComponents(target)[index]
print(("#%d %s: linked=%s first=%s second=%s value=%d"):format(
  p.index, p.type, tostring(p.linked), p.frequency.first or "-", p.frequency.second or "-", p.value))
