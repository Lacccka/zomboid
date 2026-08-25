-- Copyright (c) 2026 ReapBone. All rights reserved.

BClan = BClan or {}

BClan.LanguageText = {
    EN = {
        BClan_Title = "Advanced Clans",
        BClan_Tooltip = "Clan management",
        BClan_Loading = "Loading clan data...",
        BClan_Close = "Close",
        BClan_Name = "Clan name",
        BClan_Tag = "Clan tag",
        BClan_TagColor = "Tag color",
        BClan_Create = "Create clan",
        BClan_CreateHint = "Names: 3-15 characters. Tags: 2-4 letters or numbers.",
        BClan_InviteTitle = "You have received a clan invitation",
        BClan_Accept = "Accept",
        BClan_Decline = "Decline",
        BClan_Owner = "Owner",
        BClan_OwnerSuffix = "(Owner)",
        BClan_Level = "Clan level",
        BClan_MemberLimit = "Member limit",
        BClan_Members = "Members",
        BClan_Allies = "Allied clans",
        BClan_AllyRequests = "Alliance requests",
        BClan_Invite = "Send invite",
        BClan_RemoveMember = "Remove member",
        BClan_AddAlly = "Add ally",
        BClan_RemoveAlly = "Remove alliance",
        BClan_Leave = "Leave clan",
        BClan_PvpOn = "Clan friendly fire: ON",
        BClan_PvpOff = "Clan friendly fire: OFF",
        BClan_MaxLevel = "MAX LEVEL",
        BClan_Ranking = "Clan ranking",
        BClan_RankingHint = "Clans are ranked by lifetime total XP.",
        BClan_RankingXP = "Total XP",
        BClan_RankingEmpty = "No clans have been registered yet.",
        BClan_Back = "Back",
        BClan_Notice_AlreadyInClan = "You are already in a clan.",
        BClan_Notice_InvalidName = "Invalid clan name. Use 3-15 letters, numbers, spaces, _ or -.",
        BClan_Notice_InvalidTag = "Invalid tag. Use 2-4 letters or numbers.",
        BClan_Notice_NameUsed = "That clan name is already in use.",
        BClan_Notice_TagUsed = "That clan tag is already in use.",
        BClan_Notice_TagColorChanged = "Clan tag color changed.",
        BClan_Notice_FactionDisabled = "Faction creation is disabled or you have not survived long enough.",
        BClan_Notice_Failed = "The clan action failed. Check the server log.",
        BClan_Notice_Created = "Clan created.",
        BClan_Notice_NotOwner = "Only the clan owner can do that.",
        BClan_Notice_ClanFull = "The clan has reached its current member limit.",
        BClan_Notice_InvalidPlayer = "Select or enter a valid player.",
        BClan_Notice_PlayerOffline = "That player is not online.",
        BClan_Notice_PlayerHasClan = "That player is already in a clan.",
        BClan_Notice_InviteSent = "Clan invitation sent.",
        BClan_Notice_InviteMissing = "That invitation is no longer valid.",
        BClan_Notice_InviteAccepted = "Clan invitation accepted.",
        BClan_Notice_InviteDeclined = "Clan invitation declined.",
        BClan_Notice_InviteReceived = "You received a clan invitation. Open the clan window.",
        BClan_Notice_MemberRemoved = "Member removed from the clan.",
        BClan_Notice_NoClan = "You are not in a clan.",
        BClan_Notice_OwnerCannotLeave = "The owner cannot leave the clan.",
        BClan_Notice_LeftClan = "You left the clan.",
        BClan_Notice_PvpEnabled = "Clan friendly fire enabled.",
        BClan_Notice_PvpDisabled = "Clan friendly fire disabled.",
        BClan_Notice_InvalidClan = "Enter or select a valid clan.",
        BClan_Notice_AlreadyAllied = "Those clans are already allied.",
        BClan_Notice_AllyRequestSent = "Alliance request sent.",
        BClan_Notice_AllyRequestReceived = "Your clan received an alliance request.",
        BClan_Notice_RequestMissing = "That alliance request is no longer valid.",
        BClan_Notice_AllyAccepted = "Alliance established.",
        BClan_Notice_AllyDeclined = "Alliance request declined.",
        BClan_Notice_AllyRemoved = "Alliance removed.",
        BClan_Notice_LevelUp = "Your clan reached level",
    },
    TR = {
        BClan_Title = "Gelismis Klanlar",
        BClan_Tooltip = "Klan yonetimi",
        BClan_Loading = "Klan verileri yukleniyor...",
        BClan_Close = "Kapat",
        BClan_Name = "Klan adi",
        BClan_Tag = "Klan etiketi",
        BClan_TagColor = "Etiket rengi",
        BClan_Create = "Klan olustur",
        BClan_CreateHint = "Ad: 3-15 karakter. Etiket: 2-4 harf veya rakam.",
        BClan_InviteTitle = "Bir klan daveti aldin",
        BClan_Accept = "Kabul et",
        BClan_Decline = "Reddet",
        BClan_Owner = "Lider",
        BClan_OwnerSuffix = "(Lider)",
        BClan_Level = "Klan seviyesi",
        BClan_MemberLimit = "Uye siniri",
        BClan_Members = "Uyeler",
        BClan_Allies = "Dost klanlar",
        BClan_AllyRequests = "Dostluk istekleri",
        BClan_Invite = "Davet gonder",
        BClan_RemoveMember = "Uyeyi cikar",
        BClan_AddAlly = "Dost ekle",
        BClan_RemoveAlly = "Dostlugu kaldir",
        BClan_Leave = "Klandan ayril",
        BClan_PvpOn = "Klan ici PvP: ACIK",
        BClan_PvpOff = "Klan ici PvP: KAPALI",
        BClan_MaxLevel = "MAKSIMUM SEVIYE",
        BClan_Ranking = "Klan siralamasi",
        BClan_RankingHint = "Klanlar omur boyu toplam XP miktarina gore siralanir.",
        BClan_RankingXP = "Toplam XP",
        BClan_RankingEmpty = "Henuz kayitli bir klan yok.",
        BClan_Back = "Geri",
        BClan_Notice_AlreadyInClan = "Zaten bir klandasin.",
        BClan_Notice_InvalidName = "Gecersiz klan adi. 3-15 harf, rakam, bosluk, _ veya - kullan.",
        BClan_Notice_InvalidTag = "Gecersiz etiket. 2-4 harf veya rakam kullan.",
        BClan_Notice_NameUsed = "Bu klan adi zaten kullaniliyor.",
        BClan_Notice_TagUsed = "Bu klan etiketi zaten kullaniliyor.",
        BClan_Notice_TagColorChanged = "Klan etiket rengi degistirildi.",
        BClan_Notice_FactionDisabled = "Faction olusturma kapali veya gereken sure kadar hayatta kalmadin.",
        BClan_Notice_Failed = "Klan islemi basarisiz. Sunucu kaydini kontrol et.",
        BClan_Notice_Created = "Klan olusturuldu.",
        BClan_Notice_NotOwner = "Bu islemi yalnizca klan lideri yapabilir.",
        BClan_Notice_ClanFull = "Klan mevcut uye sinirina ulasti.",
        BClan_Notice_InvalidPlayer = "Gecerli bir oyuncu sec veya gir.",
        BClan_Notice_PlayerOffline = "Bu oyuncu cevrimici degil.",
        BClan_Notice_PlayerHasClan = "Bu oyuncu zaten bir klanda.",
        BClan_Notice_InviteSent = "Klan daveti gonderildi.",
        BClan_Notice_InviteMissing = "Bu davet artik gecerli degil.",
        BClan_Notice_InviteAccepted = "Klan daveti kabul edildi.",
        BClan_Notice_InviteDeclined = "Klan daveti reddedildi.",
        BClan_Notice_InviteReceived = "Bir klan daveti aldin. Klan penceresini ac.",
        BClan_Notice_MemberRemoved = "Uye klandan cikarildi.",
        BClan_Notice_NoClan = "Bir klanda degilsin.",
        BClan_Notice_OwnerCannotLeave = "Klan lideri klandan ayrilamaz.",
        BClan_Notice_LeftClan = "Klandan ayrildin.",
        BClan_Notice_PvpEnabled = "Klan ici PvP acildi.",
        BClan_Notice_PvpDisabled = "Klan ici PvP kapatildi.",
        BClan_Notice_InvalidClan = "Gecerli bir klan gir veya sec.",
        BClan_Notice_AlreadyAllied = "Bu klanlar zaten dost.",
        BClan_Notice_AllyRequestSent = "Dostluk istegi gonderildi.",
        BClan_Notice_AllyRequestReceived = "Klanin bir dostluk istegi aldi.",
        BClan_Notice_RequestMissing = "Bu dostluk istegi artik gecerli degil.",
        BClan_Notice_AllyAccepted = "Klan dostlugu kuruldu.",
        BClan_Notice_AllyDeclined = "Dostluk istegi reddedildi.",
        BClan_Notice_AllyRemoved = "Klan dostlugu kaldirildi.",
        BClan_Notice_LevelUp = "Klanin su seviyeye ulasti:",
    },
}

function BClan.getLanguage()
    local player = getPlayer and getPlayer() or nil
    local playerKey = player and player:getUsername() or "__none"
    if BClan.LanguagePlayer ~= playerKey or not BClan.SelectedLanguage then
        BClan.LanguagePlayer = playerKey
        local saved = player and player:getModData().BClanLanguage or nil
        BClan.SelectedLanguage = (saved == "TR" or saved == "EN") and saved or (BClan.Config.DefaultLanguage or "EN")
    end
    return BClan.SelectedLanguage
end

function BClan.setLanguage(language)
    if language ~= "TR" and language ~= "EN" then return end
    BClan.SelectedLanguage = language
    local player = getPlayer and getPlayer() or nil
    if player then
        BClan.LanguagePlayer = player:getUsername()
        player:getModData().BClanLanguage = language
        pcall(function() player:transmitModData() end)
    end
end

function BClan.text(key)
    local language = BClan.getLanguage()
    local selected = BClan.LanguageText[language] or BClan.LanguageText.EN
    return selected[key] or BClan.LanguageText.EN[key] or getText(key)
end
