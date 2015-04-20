package ch.eawag.chemeql;


class ModelEnum extends Object
{
	static final ModelEnum CONSTANT_CAP = new ModelEnum("ConstantCap");
	static final ModelEnum DIFFUSE_GTL = new ModelEnum("Diffuse/GTL");
	static final ModelEnum STERN_L = new ModelEnum("SternL");
	static final ModelEnum TRIPLE_L = new ModelEnum("TripleL");

	static ModelEnum getInstance(String n) {
		if (n.equals(CONSTANT_CAP.getName())) {
			return CONSTANT_CAP;
		}
		if (n.equals(DIFFUSE_GTL.getName())) {
			return DIFFUSE_GTL;
		}
		if (n.equals(STERN_L.getName())) {
			return STERN_L;
		}
		if (n.equals(TRIPLE_L.getName())) {
			return TRIPLE_L;
		}
		return null;
	}

	private String name;

	private ModelEnum(String n) {
		name = n;
	}

	String getName() {
		return name;
	}
}
