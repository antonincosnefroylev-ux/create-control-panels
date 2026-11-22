-- ccwatch: prints every console_signal event, with the console it came from.
-- Usage: ccwatch          Ctrl+T stops.
-- Checks: clicking parts on ANY of the joined consoles produces an event, the
-- source console (name or position) is right, and a console standing apart
-- produces nothing.

local console = peripheral.find("control_console")
if not console then error("No control_console found: put the computer against a console", 0) end

print("Watching console_signal... (Ctrl+T to stop)")
while true do
  local _, side, index, value, source = os.pullEvent("console_signal")
  local from = source.name or ("%d,%d,%d"):format(source.x, source.y, source.z)
  print(("[%6.1f] %s: %s #%d = %d"):format(os.clock(), side, from, index, value))
end
