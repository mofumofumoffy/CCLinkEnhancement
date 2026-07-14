local upgrades = link.getUpgradeFunctions()
local overlay = upgrades.enhanced_overlay

overlay.clearAll()

overlay.addOrUpdateTextElement("scalableItemLabel", "Scalable Item", 1, 1, 0xffffff, 2)

overlay.addOrUpdateTextElement("scalableItemLabel_normal", "Normal", 1, 24, 0xffffff, 1)
overlay.addOrUpdateItemElement("item_normal", "minecraft:diamond", 1, 32)

overlay.addOrUpdateTextElement("scalableItemLabel_scalable", "Scalable", 64, 24, 0xffffff, 1)

--[[
    args:{
        key:string,
        itemIdWithNbt:string,
        x:number,
        y:number,
        scale:number
    }

]]
overlay.addOrUpdateScalableItemElement("item_scalable", "minecraft:diamond{tag:1b}", 64, 32, 3)

overlay.addOrUpdateTextElement("formattedTextLabel", "Formatted Text", 1, 81, 0xffffff, 2)

--[[
    args:{
        key:string,
        text:string(Supports Formatting Codes),
        x: number,
        y: number,
        defaultColor: number,
        scale: number,
        formats: {
            obfuscated:boolean or nil,
            bold:boolean or nil,
            strikethrough:boolean or nil,
            underlined:boolean or nil,
            italic:boolean or nil,
            fontName:ResourceLocation or nil
        } or nil
    }
]]

overlay.addOrUpdateFormattedTextElement("formattedText", "Hello, World!", 5, 98, 0xffffff, 2, {underlined=true, italic=true})

overlay.addOrUpdateTextElement("entityLabel", "Entity", 1, 124, 0xffffff, 2)

--[[
    args:{
        key:string,
        entityUUID:uuid or nil(Setting it to nil will draw yourself.),
        rotation:{yaw:number, roll:number, pitch:number} or {xRot:number, yRot:number, zRot:number},
        x:number,
        y:number,
        scale:number
    }
]]
overlay.addOrUpdateEntityElement("entity_self", nil, {yRot = math.pi / 4, xRot = math.atan(1 / math.sqrt(2))}, 1, 138, 2)

overlay.addOrUpdateTextElement("textureLabel", "Texture", 1, 182, 0xffffff, 2)

--[[
    args:{
        key:string,
        textureLoc:ResourceLocation(Place the textures in `textures/gui/hud_elements/*`),
        uWidth:number,
        vHeight:number,
        x:number,
        y:number,
        scale:number
    }
]]
overlay.addOrUpdateTextureElement("bar_under", "cclink_enhancement:progress_bar_under", 37, 9, 1, 202, 2)
overlay.addOrUpdateTextureElement("bar", "cclink_enhancement:progress_bar", 18, 9, 1, 202, 2)


overlay.send()
