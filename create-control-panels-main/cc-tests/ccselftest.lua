-- ccselftest: automatic checks of the peripheral's rules. Prints PASS/FAIL.
-- Usage: ccselftest
-- It briefly changes component #1's frequency on the touched console and puts
-- it back, and sweeps one unlinked lamp/meter if there is one.

local console = peripheral.find("control_console")
if not console then error("No control_console found: put the computer against a console", 0) end

local passed, failed = 0, 0
local function check(ok, what)
  if ok then passed = passed + 1 else failed = failed + 1 end
  print((ok and "PASS " or "FAIL ") .. what)
end
local function refuses(what, fn, ...)
  local ok, err = pcall(fn, ...)
  check(not ok, what .. (ok and "" or ("  (" .. tostring(err) .. ")")))
end

-- the group
local consoles = console.getConsoles()
local attached = 0
for _, c in ipairs(consoles) do if c.attached then attached = attached + 1 end end
check(#consoles >= 1 and consoles[1].attached and attached == 1, "touched console listed first, exactly once")
print(("     (%d console(s) in the group)"):format(#consoles))

-- every entry selects its own console, by table and by name
local names = {}
for _, c in ipairs(consoles) do
  check(#console.getComponents(c) == c.components, ("table selects console @ %d,%d,%d"):format(c.x, c.y, c.z))
  if c.name then names[c.name] = (names[c.name] or 0) + 1 end
end
for _, c in ipairs(consoles) do
  if c.name and names[c.name] == 1 then
    check(#console.getComponents(c.name) == c.components, "name selects '" .. c.name .. "'")
  elseif c.name then
    refuses("shared name '" .. c.name .. "' refused", console.getComponents, c.name)
  end
end
check(#console.getComponents() == consoles[1].components, "no console argument = touched console")

-- bad selectors and arguments
local here = consoles[1]
refuses("unknown name refused", console.getComponents, "No Such Console 123")
refuses("position outside the group refused", console.getComponents, { x = here.x + 500, y = here.y, z = here.z })
refuses("table without x/y/z or name refused", console.getComponents, { foo = 1 })
refuses("component 0 refused", console.getValue, 0)
refuses("component past the end refused", console.getValue, here.components + 1)

local parts = console.getComponents()
if #parts == 0 then
  print("SKIP (touched console has no components)")
else
  refuses("value 16 refused", console.setValue, 1, 16)
  refuses("value -1 refused", console.setValue, 1, -1)

  -- frequencies: set, read back, clear, restore
  local orig = parts[1].frequency
  refuses("unknown item refused", console.setFrequency, 1, "minecraft:not_an_item", nil)
  refuses("air refused", console.setFrequency, 1, "minecraft:air", nil)
  refuses("non-string item refused", console.setFrequency, 1, 42, nil)

  console.setFrequency(1, "minecraft:stick", nil)
  local p = console.getComponents()[1]
  check(p.linked and p.frequency.first == "minecraft:stick" and p.frequency.second == nil,
    "setFrequency(stick, nil) reads back")
  console.setFrequency(1, nil, nil)
  check(not console.getComponents()[1].linked, "setFrequency(nil, nil) unlinks")
  console.setFrequency(1, orig.first, orig.second)
  local back = console.getComponents()[1].frequency
  check(back.first == orig.first and back.second == orig.second, "original frequency restored")
end

-- an unlinked lamp/meter anywhere in the group
local done = false
for _, c in ipairs(consoles) do
  for _, q in ipairs(console.getComponents(c)) do
    if not done and not q.linked and q.direction == "receive" then
      console.setValue(q.index, 9, c)
      check(console.getValue(q.index, c) == 9, ("unlinked %s #%d shows 9"):format(q.type, q.index))
      console.setValue(q.index, 0, c)
      check(console.getValue(q.index, c) == 0, ("unlinked %s #%d back to 0"):format(q.type, q.index))
      done = true
    end
  end
end
if not done then print("SKIP (no unlinked lamp/meter in the group)") end

print(("%d passed, %d failed"):format(passed, failed))
