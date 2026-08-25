local OutfitCombo = {}

local function trim(s)
	return tostring(s or ""):match("^%s*(.-)%s*$") or ""
end

function OutfitCombo.populate(combo)
	combo.options = {}
	combo.selected = 0
	combo:addOptionWithData(getText("UI_characreation_clothing_none"), "")
	if not getAllOutfits then
		combo.selected = 1
		return
	end
	local maleOutfits = getAllOutfits(false)
	local femaleOutfits = getAllOutfits(true)
	if not maleOutfits or not femaleOutfits then
		combo.selected = 1
		return
	end
	for i = 0, maleOutfits:size() - 1 do
		local name = maleOutfits:get(i)
		local suffix = ""
		if not femaleOutfits:contains(name) then
			suffix = getText("IGUI_INPC_OutfitMaleOnlySuffix")
		end
		combo:addOptionWithData(name .. suffix, name)
	end
	for i = 0, femaleOutfits:size() - 1 do
		local name = femaleOutfits:get(i)
		if not maleOutfits:contains(name) then
			combo:addOptionWithData(name .. getText("IGUI_INPC_OutfitFemaleOnlySuffix"), name)
		end
	end
	combo.selected = 1
end

--- if a saved outfit string is not in the game list (mod removed, typo), keep it selectable.
function OutfitCombo.ensureUnknownOutfitOption(combo, outfitName)
	outfitName = trim(outfitName)
	if outfitName == "" then
		return
	end
	for i = 1, #(combo.options or {}) do
		if combo:getOptionData(i) == outfitName then
			return
		end
	end
	combo:addOptionWithData(outfitName, outfitName)
end

function OutfitCombo.selectByOutfitName(combo, outfitName)
	outfitName = trim(outfitName)
	if outfitName == "" then
		combo.selected = combo.options and #combo.options > 0 and 1 or 0
		return
	end
	for i = 1, #(combo.options or {}) do
		if combo:getOptionData(i) == outfitName then
			combo.selected = i
			return
		end
	end
	combo.selected = 1
end

function OutfitCombo.getSelectedOutfitName(combo)
	if not combo or combo.selected < 1 then
		return ""
	end
	local data = combo:getOptionData(combo.selected)
	return trim(data or "")
end

return OutfitCombo
