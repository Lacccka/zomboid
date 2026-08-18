-- COMPLETE copy of upstream AWCWF_KeyBind.lua plus one added binding.
-- Lua overrides are whole-file by path: every upstream binding must stay here or
-- it disappears from the key-settings screen.
--
-- Added by the community fix: SwitchFireModeCat (see MFSFireModeHotkey.lua).

local bind = {};
bind.value = "[KeySetting_AWCWF]";
table.insert(keyBinding, bind);

bind = {};
bind.value = "LauchGrenadelauncherat";
bind.key = Keyboard.KEY_G;
table.insert(keyBinding, bind);

bind = {};
bind.value = "OpenWindownCat";
bind.key = Keyboard.KEY_T;
table.insert(keyBinding, bind);

bind = {};
bind.value = "GunLineDown";
bind.key = Keyboard.KEY_Z;
table.insert(keyBinding, bind);

bind = {};
bind.value = "SwtichLightSet";
bind.key = Keyboard.KEY_F;
table.insert(keyBinding, bind);

-- Community fix addition. KEY_BACK is unbound in vanilla B42 and does not collide
-- with the four bindings above. Fully reconfigurable in Options > Key Bindings.
bind = {};
bind.value = "SwitchFireModeCat";
bind.key = Keyboard.KEY_BACK;
table.insert(keyBinding, bind);
