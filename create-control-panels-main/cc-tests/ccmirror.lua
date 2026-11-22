-- ccmirror: copies one component's value onto a component of ANOTHER joined console.
-- Usage: ccmirror <from console> <from #> <to console> <to #>
--   Console is a name set in the designer, or "-" for the touched console.
--   Example: ccmirror - 1 Reactor 2   (switch #1 here lights lamp #2 on "Reactor")
-- Ctrl+T stops.

local console = peripheral.find("control_console")
if not console then error("No control_console found: put the computer against a console", 0) end

local fromName, fromIndex, toName, toIndex = arg[1], tonumber(arg[2]), arg[3], tonumber(arg[4])
if not (fromName and fromIndex and toName and toIndex) then
  error("Usage: ccmirror <from console|-> <from #> <to console|-> <to #>", 0)
end
local function pick(name) if name == "-" then return nil end return name end
local from, to = pick(fromName), pick(toName)

-- the source console's position, to match events against
local src
for _, c in ipairs(console.getConsoles()) do
  if (from == nil and c.attached) or (from ~= nil and c.name == from) then src = c end
end
if not src then error("No joined console named " .. fromName, 0) end

local value = console.getValue(fromIndex, from)
console.setValue(toIndex, value, to)
print(("Start: %s #%d = %d -> %s #%d"):format(fromName, fromIndex, value, toName, toIndex))

while true do
  local _, _, index, v, source = os.pullEvent("console_signal")
  if index == fromIndex and source.x == src.x and source.y == src.y and source.z == src.z then
    console.setValue(toIndex, v, to)
    print(("  %d -> %s #%d now %d"):format(v, toName, toIndex, console.getValue(toIndex, to)))
  end
end
