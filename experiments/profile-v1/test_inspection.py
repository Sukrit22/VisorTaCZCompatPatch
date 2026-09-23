import json
import tempfile
import unittest
import zipfile
from pathlib import Path
from inspect_packs import Pack

class InspectionTests(unittest.TestCase):
    def test_folder_and_wrapped_zip_match(self):
        with tempfile.TemporaryDirectory() as t:
            p=Path(t)/'pack';p.mkdir();(p/'gunpack.meta.json').write_text('{"namespace":"test"}')
            data=Pack(p).inspect();z=Path(t)/'pack.zip'
            with zipfile.ZipFile(z,'w') as f:f.write(p/'gunpack.meta.json','wrapper/gunpack.meta.json')
            self.assertEqual(data['guns'],Pack(z).inspect()['guns'])
    def test_comments_do_not_remove_urls(self):
        with tempfile.TemporaryDirectory() as t:
            p=Path(t);(p/'sample.json').write_text('{"url":"https://example.test/x", /* note */ "x":1 // end\n}')
            self.assertEqual('https://example.test/x',Pack(p).read('sample.json')['url'])
    def test_lenient_literals_are_reported(self):
        with tempfile.TemporaryDirectory() as t:
            p=Path(t);(p/'sample.json').write_text('{"shadow":flase,"knockback":0.}')
            pack=Pack(p);data=pack.read('sample.json');self.assertEqual('flase',data['shadow']);self.assertEqual(2,len(pack.warnings))

if __name__=='__main__':unittest.main()
