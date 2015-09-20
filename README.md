# Filtrin

Filtrin is a filtering and statistics software application designed for the processing of ATOM and HETATM records contained within Protein Data Bank (.pdb) files. Filtrin currently focuses on the extraction and manipulation of the temperature factor (or Beta-Factor) values of these records, although a future update may expand Filtrin's feature set. Filtrin works by first reading and collecting relevant information from each line of the .pdb file, including: temperature factor values, atom names, residue names, residue sequence numbers, and chain Ids. Afterwards, Filtrin sorts the data according to the chains present, and then into subcategories such as: Residue atoms, Main chain atoms, Backbone atoms, C-Alpha atoms, and Side chain atoms. Note: By default, Filtrin will filter out all hydrogen atoms from the data pool. If you wish to include hydrogen atoms in the calculations, simple toggle the "Include Hydrogen Atoms" button. 

Once the values have been sorted and filtered, Filtrin will compute temperature factor averages on a per residue basis, while also taking the different categories into consideration. This means that if the category is "Main Chain", for instance, then only the values from the N, CA, C and O atoms will be averaged, and it is that number that will used for further calculations, including generating series for the chart, and statistics for the table. Note: the "Normalize Values" toggle button is selected by default, and normalization is based on the averages (i.e.: not the individual temperature factor values). If you wish to normalize values, on a per chain and per atom basis, then you may utilize Filtrin's export functionality.

Filtrin has the ability to export temperature factor data to a Microsoft Excel spreadsheet (.xlsx), in a per chain arrangement (i.e.: chains are separated into sections, which are composed of multiple vertical columns). To export, simply right click on a file in the statistics table, choose the export option, and specify the directory the spreadsheet file will be saved to. Note: Filtrin will export data depending on what category is selected. Given that, Filtrin will draw from the master collection pool and reapply filters to match the settings (again, on a per chain basis). The normalized values that are present in the spreadsheet are calculated from the individual atoms, which may give different results on than what is seen on the chart, which are averages.

Filtrin utilizes Apache Commons Math for calculating the statistics and normalizing the data. Google Guava is used for storing and filtering data extracted from the .pdb files. ControlsFX is used for the chart sliders and the chain Id toggles, while Google's Material Design Icons are used for the program's icon set. Finally, Apache POI is used for the export to excel function.
