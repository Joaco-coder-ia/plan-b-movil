"""Prueba de humo contra servicios levantados. Crea y elimina solo sus propios registros."""
import json
import urllib.request
from pathlib import Path

def request(method,url,data=None):
    body=None if data is None else json.dumps(data).encode('utf-8')
    req=urllib.request.Request(url,data=body,method=method,headers={'Content-Type':'application/json'})
    with urllib.request.urlopen(req,timeout=20) as response:
        text=response.read()
        return response.status,json.loads(text) if text else None

checks=[]
cases=[('http://localhost:8081/api/lugares',{'nombre':'Prueba HTTP temporal','descripcion':'Registro creado para verificar CRUD real','costo':0,'minutos':30,'interior':True,'latitud':-33.45,'longitud':-70.66},'nombre'),
       ('http://localhost:8082/api/planes',{'titulo':'Prueba HTTP temporal','lugarNombre':'Biblioteca de ejemplo','fecha':'2026-12-01','estado':'PENDIENTE','notas':'','valoracion':0},'titulo')]
for url,payload,field in cases:
    status,created=request('POST',url,payload)
    assert status==201
    location=url+'/'+str(created['id'])
    try:
        assert request('GET',location)[1][field]==payload[field]
        payload[field]='Prueba HTTP modificada'
        assert request('PUT',location,payload)[1][field]==payload[field]
        assert any(x['id']==created['id'] for x in request('GET',url)[1])
    finally:
        assert request('DELETE',location)[0]==204
    checks.append({'servicio':url,'CRUD':'aprobado'})
status,weather=request('GET','https://api.open-meteo.com/v1/forecast?latitude=-33.45&longitude=-70.66&current=temperature_2m,precipitation,weather_code')
assert 'temperature_2m' in weather['current']
checks.append({'api':'Open-Meteo','estado':status,'hora_proveedor':weather['current']['time']})
out=Path(__file__).resolve().parents[1]/'docs/evidencias/http.json'
out.parent.mkdir(parents=True,exist_ok=True)
out.write_text(json.dumps(checks,indent=2,ensure_ascii=False),encoding='utf-8')
print('CRUD real de ambos servicios y respuesta externa: aprobados.')
