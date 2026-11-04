import importlib.util,json,tempfile,unittest,zipfile
from pathlib import Path
spec=importlib.util.spec_from_file_location('catalog',Path(__file__).with_name('catalog-gun-parts.py'))
m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)

class CatalogueTest(unittest.TestCase):
    def fixture(self,root):
        files={
            'data/test/index/guns/example.json':{'display':'test:base','type':'pistol'},
            'assets/test/display/guns/base.json':{'model':'other:gun/body','animation':'test:motion','lod':{'model':'other:lod/body'}},
            'assets/test/display/guns/variant.json':{'model':'other:gun/body'},
            'assets/other/geo_models/gun/body.json':{'minecraft:geometry':[{'description':{'identifier':'body'},'bones':[{'name':'root','pivot':[0,0,0]},{'name':'Bolt','parent':'root','cubes':[{}]}]}]},
            'assets/test/animations/motion.animation.json':{'animations':{'shoot':{'bones':{'Bolt':{'position':[0,0,1]}}}}}
        }
        for name,data in files.items():
            p=root/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(data),encoding='utf-8')
    def test_directory_zip_and_variant_equivalence(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp)/'pack';self.fixture(root);a=m.collect(root)
            archive=Path(tmp)/'pack.zip'
            with zipfile.ZipFile(archive,'w') as z:
                for p in root.rglob('*.json'):z.write(p,p.relative_to(root).as_posix())
            b=m.collect(archive)
            self.assertEqual(a['displays'],b['displays']);self.assertEqual(a['sha256'],b['sha256'])
            self.assertEqual(len(a['guns']),1);self.assertEqual(len(a['displays']),2)
            bolt=a['displays'][0]['geometries'][0]['bones'][1]
            self.assertEqual(bolt['name'],'Bolt');self.assertEqual(bolt['parent'],'root')
            self.assertEqual(bolt['animation_channels'],{'shoot':['position']})
            self.assertEqual(a['warnings'],[])
    def test_missing_resource_reported_not_invented(self):
        with tempfile.TemporaryDirectory() as tmp:
            root=Path(tmp);self.fixture(root)
            (root/'assets/other/geo_models/gun/body.json').unlink()
            r=m.collect(root);self.assertEqual(len(r['warnings']),2)
            self.assertEqual(r['displays'][0]['geometries'],[])
    def test_json_comments_preserve_urls(self):
        self.assertEqual(m.json_data(b'{/*c*/"url":"https://x//y",//x\n"x":1}'),{'url':'https://x//y','x':1})
    def test_resource_validation(self):
        for ref in ['../bad','test:../outside','no_namespace']:
            with self.assertRaises(ValueError):m.resource(ref,'geo_models')
    def test_wrong_root_rejected(self):
        with tempfile.TemporaryDirectory() as tmp:
            with self.assertRaisesRegex(ValueError,'pack root'):m.collect(tmp)

if __name__=='__main__':unittest.main()
