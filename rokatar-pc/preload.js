const { contextBridge, ipcRenderer } = require('electron');
contextBridge.exposeInMainWorld('RokatarPC',{
  listGames:()=>ipcRenderer.invoke('games:list'),
  addGame:()=>ipcRenderer.invoke('games:add'),
  removeGame:(id)=>ipcRenderer.invoke('games:remove',id),
  launchGame:(id)=>ipcRenderer.invoke('games:launch',id)
});
