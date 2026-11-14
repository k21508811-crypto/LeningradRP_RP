const { app, BrowserWindow, ipcMain, dialog } = require('electron');
const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');
let win;
const dataFile = () => path.join(app.getPath('userData'), 'games.json');
function readGames(){ try{return JSON.parse(fs.readFileSync(dataFile(),'utf8'))}catch{return []} }
function writeGames(games){ fs.mkdirSync(path.dirname(dataFile()),{recursive:true});fs.writeFileSync(dataFile(),JSON.stringify(games,null,2)); }
function createWindow(){
  win = new BrowserWindow({width:1180,height:780,minWidth:760,minHeight:560,backgroundColor:'#0c0c0e',autoHideMenuBar:true,webPreferences:{preload:path.join(__dirname,'preload.js'),contextIsolation:true,nodeIntegration:false}});
  win.loadFile('index.html');
}
app.whenReady().then(()=>{createWindow();app.on('activate',()=>{if(BrowserWindow.getAllWindows().length===0)createWindow()})});
app.on('window-all-closed',()=>{if(process.platform!=='darwin')app.quit()});
ipcMain.handle('games:list',()=>readGames());
ipcMain.handle('games:add',async()=>{
  const result=await dialog.showOpenDialog(win,{title:'Выбери EXE-файл игры',properties:['openFile'],filters:[{name:'Windows application',extensions:['exe']}]});
  if(result.canceled||!result.filePaths[0])return readGames();
  const exe=result.filePaths[0], games=readGames();
  if(!games.some(g=>g.path.toLowerCase()===exe.toLowerCase()))games.push({id:Date.now().toString(),name:path.basename(exe,'.exe'),path:exe});
  writeGames(games);return games;
});
ipcMain.handle('games:remove',(_,id)=>{const games=readGames().filter(g=>g.id!==id);writeGames(games);return games});
ipcMain.handle('games:launch',(_,id)=>{
  const game=readGames().find(g=>g.id===id);if(!game)return {ok:false,error:'Игра не найдена'};
  if(!fs.existsSync(game.path))return {ok:false,error:'Файл игры перемещён или удалён'};
  try{spawn(game.path,[],{detached:true,stdio:'ignore',cwd:path.dirname(game.path),windowsHide:false}).unref();return {ok:true}}catch(e){return {ok:false,error:e.message}}
});
