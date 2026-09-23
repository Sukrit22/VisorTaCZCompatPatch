import copy
import tomllib
import unittest
from pathlib import Path
from validate import validate, ProfileError

class SchemaTests(unittest.TestCase):
    def setUp(self):
        self.doc=tomllib.loads((Path(__file__).parent/'examples/immersive-pistol.toml').read_text())
    def test_examples(self):
        for p in (Path(__file__).parent/'examples').glob('*.toml'):validate(tomllib.loads(p.read_text()))
    def test_bad_reference(self):
        self.doc['guns'][0]['bindings']['rack']['joint']='missing'
        with self.assertRaises(ProfileError):validate(self.doc)
    def test_duplicate_identity(self):
        self.doc['guns'].append(copy.deepcopy(self.doc['guns'][0]))
        with self.assertRaises(ProfileError):validate(self.doc)
    def test_invalid_axis(self):
        self.doc['guns'][0]['joints']['slide_travel']['axis']=[0,0,float('nan')]
        with self.assertRaises(ProfileError):validate(self.doc)
    def test_reversed_limits(self):
        self.doc['guns'][0]['joints']['slide_travel']['limits']=[1,0]
        with self.assertRaises(ProfileError):validate(self.doc)
    def test_visual_cannot_mutate_ammo(self):
        self.doc['guns'][0]['poses'][0]['state']='ammo.add'
        with self.assertRaises(ProfileError):validate(self.doc)
    def test_unknown_ammo_contract(self):
        self.doc['guns'][0]['ammo_adapter']='plasma_charge'
        with self.assertRaises(ProfileError):validate(self.doc)
    def test_unbounded_zone(self):
        self.doc['guns'][0]['zones']['rack']['size']=[-1,1,1]
        with self.assertRaises(ProfileError):validate(self.doc)

if __name__=='__main__':unittest.main()
