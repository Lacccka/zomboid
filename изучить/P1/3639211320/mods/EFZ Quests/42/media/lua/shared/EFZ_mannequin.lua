require "Scripting/MFManager"

local example_1 = {
    name = "example_normal",
    script = "Base.MaleFolk",
    haircut = { "RightParting", {0.10588235408067703,0.09019608050584793,0.08627451211214066} },
    beard = "Moustache",
    clothes = {
        { "Glasses_Normal", 0 },
        { "Shirt_FormalTINT", {1,1,1} },
        { "Socks_Ankle", {0.2705882489681244,0.25882354378700256,0.5137255191802979} },
        { "Trousers_DefaultTEXTURE_TINT", {0,0,0} },
        { "Shoes_TrainerTINT", {0.42352941632270813,0.4117647111415863,0.46666666865348816} },
        "TorsoExtra_Apron_Black",
    }
};

table.insert(MFManager.templates, example_1);

local bartender_1 = {
    name = "bartender_normal",
    script = "Base.MaleFolk",
    haircut = { "RightParting", {0.10588235408067703,0.09019608050584793,0.08627451211214066} },
    beard = "Moustache",
    clothes = {
        { "Glasses_Normal", 0 },
        "Tshirt_WhiteLongSleeve",
        "Trousers_Suit",
        "Shoes_Black",
        "Tie_BowTieWorn",
        "Vest_Waistcoat",
    }
};
table.insert(MFManager.templates, bartender_1);

local captain_1 = {
    name = "captain_normal",
    script = "Base.FemaleFolk",
    haircut = { "Long2", {0.8313725590705872,0.6705882549285889,0.2705882489681244} },
    clothes = {
        "Trousers_CamoDesert",
        "Shirt_CamoDesert",
        "Shoes_ArmyBootsDesert",
    }
};

table.insert(MFManager.templates, captain_1);

local doctor_1 = {
    name = "doctor_normal",
    script = "Base.MaleFolk",
    haircut = { "Recede", {0.21960784494876862,0.16078431904315948,0.10588235408067703} },
    clothes = {
        "Shirt_FormalWhite",
        "Left_RingFinger_Silver",
        { "Socks_Ankle", {0.3843137323856354,0.6901960968971252,0.545098066329956} },
        "Trousers_SuitTEXTURE",
        "JacketLong_Doctor",
        "Shoes_Brown",
    }
};

table.insert(MFManager.templates, doctor_1);

local pilot_1 = {
    name = "pilot_normal",
    script = "Base.MaleFolk",
    haircut = { "Short", {0.6117647290229797,0.5098039507865906,0.33725491166114807} },
    clothes = {
        { "Tshirt_DefaultDECAL_TINT", {0.6980392336845398,0.6039215922355652,0.545098066329956} },
        "Necklace_DogTag",
        "Left_RingFinger_Silver",
        { "Socks_Ankle", {0.6941176652908325,0.8549019694328308,0.3450980484485626} },
        "Boilersuit_Flying",
        "Shoes_Black",
    }
};

table.insert(MFManager.templates, pilot_1);

local gunner_1 = {
    name = "gunner_normal",
    script = "Base.MaleFolk",
    haircut = { "Bald", {0.10588235408067703,0.09019608050584793,0.08627451211214066} },
    beard = "LongScruffy",
    clothes = {
        "Vest_DefaultTEXTURE",
        "Necklace_DogTag",
        "Trousers_CamoDesert",
        "Shoes_ArmyBootsDesert",
        "Glasses_Aviators",
    }
};

table.insert(MFManager.templates, gunner_1);

local designer_1 = {
    name = "designer_normal",
    script = "Base.FemaleFolk",
    haircut = { "Braids", {0.10588235408067703,0.09019608050584793,0.08627451211214066} },
    clothes = {
        { "BoobTube", {0.239215686917305,0.1882352977991104,0.26274511218070984} },
        { "Skirt_Long", {0.47843137383461,0.4588235318660736,0.6313725709915161} },
        "Shoes_Strapped",
    }
};

table.insert(MFManager.templates, designer_1);

--[[
bartender Bartender:gender=2;skincolor=0.98,0.79,0.49;name=bartender|Bartender;hair=RightParting|0.10588235408067703,0.09019608050584793,0.08627451211214066;chesthair=2;beard=Moustache;Shirt=Base.Shirt_FormalTINT|1,1,1;Socks=Base.Socks_Ankle|0.2705882489681244,0.25882354378700256,0.5137255191802979;Pants=Base.Trousers_DefaultTEXTURE_TINT|0,0,0;Shoes=Base.Shoes_TrainerTINT|0.42352941632270813,0.4117647111415863,0.46666666865348816;TorsoExtra=Base.Apron_Black;Eyes=Base.Glasses_Normal|1;

captain Captain:gender=1;skincolor=0.98,0.79,0.49;name=captain|Captain;hair=Long2|0.8313725590705872,0.6705882549285889,0.2705882489681244;Pants=Base.Military_Pants_Rolled|7;Jacket=Base.Military_Jumper|7;Shoes=Base.Shoes_ArmyBootsDesert;

doctor Doctor:gender=2;skincolor=0.8,0.65,0.45;name=doctor|Doctor;hair=Recede|0.21960784494876862,0.16078431904315948,0.10588235408067703;chesthair=2;beard=;Shirt=Base.Shirt_FormalWhite;Left_RingFinger=Base.Ring_Left_RingFinger_Silver;Socks=Base.Socks_Ankle|0.3843137323856354,0.6901960968971252,0.545098066329956;Pants=Base.Trousers_SuitTEXTURE;Jacket=Base.JacketLong_Doctor;Shoes=Base.Shoes_Brown;

pilot Pilot:gender=2;skincolor=1,0.91,0.72;name=pilot|Pilot;hair=Short|0.6117647290229797,0.5098039507865906,0.33725491166114807;chesthair=2;beard=;Tshirt=Base.Tshirt_DefaultDECAL_TINT|0.6980392336845398,0.6039215922355652,0.545098066329956;Necklace=Base.Necklace_DogTag;Left_RingFinger=Base.Ring_Left_RingFinger_Silver;Socks=Base.Socks_Ankle|0.6941176652908325,0.8549019694328308,0.3450980484485626;Boilersuit=Base.Boilersuit_Flying;Shoes=Base.Shoes_Black;

gunner Gunner:gender=2;skincolor=0.36,0.25,0.14;name=gunner|Gunner;hair=Bald|0.10588235408067703,0.09019608050584793,0.08627451211214066;chesthair=2;beard=LongScruffy;TankTop=Base.Vest_DefaultTEXTURE;Necklace=Base.Necklace_DogTag;Pants=Base.MIL_Pants_Rolled|7;Shoes=Base.Shoes_ArmyBootsDesert;Eyes=Base.Glasses_Aviators;

designer Designer:gender=1;skincolor=0.54,0.38,0.25;name=designer|Designer;hair=Braids|0.10588235408067703,0.09019608050584793,0.08627451211214066;Jacket=Base.Off_Dress|2;Shoes=Base.Shoes_BlackBoots;
]]--