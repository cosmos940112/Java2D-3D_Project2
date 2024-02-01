package starter3d;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class OBJReader {
	TriangleMesh tmesh;

	/*
	 * Read OFF files quick and dirty. For now, limited to triangle meshes.
	 */
	public boolean read_OFF(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));

			String line;
			line = br.readLine();
			Scanner sc = new Scanner(line);

			// For now ignore comments '#', TODO later

			String tmp = sc.next();
			if (!tmp.equalsIgnoreCase("OFF")) {
				sc.close();
				br.close();
				System.out.println("Incorrect OFF file format");
				return false;
			}
			sc.close();

			line = br.readLine();
			sc = new Scanner(line);
			int nv = sc.nextInt();
			int nf = sc.nextInt();
			sc.nextInt(); // can be zero so ignore it
			sc.close();

			tmesh = new TriangleMesh(nv, nf);

			double dx, dy, dz;

			for (int i = 0; i < tmesh.number_vertices; i++) {
				line = br.readLine();
				sc = new Scanner(line);

				dx = sc.nextDouble();
				dy = sc.nextDouble();
				dz = sc.nextDouble();

				if (sc.hasNext()) {
					// TODO later handle color or normal information?
				}

				tmesh.setVertex(i, dx, dy, dz);

				sc.close();
			}

			int fi, fj, fk;
			for (int i = 0; i < tmesh.number_faces; i++) {
				line = br.readLine();
				sc = new Scanner(line);

				sc.nextInt(); // dummy we assume triangle meshes
				fi = sc.nextInt();
				fj = sc.nextInt();
				fk = sc.nextInt();

				tmesh.setFace(i, fi, fj, fk);
				sc.close();
			}

			br.close();
			
//			System.out.println("Read mesh: " + filename);
//			System.out.println("made of " + tmesh.number_vertices + " vertices and " + tmesh.number_faces + " faces.");

			return true;
		} catch (IOException e) {
			System.out.println("An exception occurred while reading " + filename);
			return false;
		}
	}
	
	public boolean read_OBJ(String filename) {
	    try {
	        BufferedReader br = new BufferedReader(new FileReader(filename));

	        String line;
	        line = br.readLine();
	        Scanner sc = new Scanner(line);

	        // For now ignore comments '#', TODO later

	        String tmp = sc.next();
	        if (!tmp.equalsIgnoreCase("v")) {
	            sc.close();
	            br.close();
	            System.out.println("Incorrect OBJ file format");
	            return false;
	        }
	        sc.close();

	        int nv = 0; // Vertex count
	        int nf = 0; // Face count

	        // Count vertices and faces
	        while ((line = br.readLine()) != null) {
	            sc = new Scanner(line);
	            if (sc.hasNext()) {
	                tmp = sc.next();
	                if (tmp.equalsIgnoreCase("v")) {
	                    nv++;
	                } else if (tmp.equalsIgnoreCase("f")) {
	                    nf++;
	                }
	            }
	            sc.close();
	        }

	        br.close();

	        // Reopen the file for actual reading
	        br = new BufferedReader(new FileReader(filename));

	        // Skip comments and vertex data to reach face data
	        while ((line = br.readLine()) != null && !line.trim().startsWith("f")) {}

	        tmesh = new TriangleMesh(nv, nf);

	        for (int i = 0; i < tmesh.number_vertices; i++) {
	            line = br.readLine();
	            sc = new Scanner(line);

	            // Skip "v"
	            sc.next();

	            double dx = sc.nextDouble();
	            double dy = sc.nextDouble();
	            double dz = sc.nextDouble();

	            if (sc.hasNext()) {
	                // TODO later handle color or normal information?
	            }

	            tmesh.setVertex(i, dx, dy, dz);

	            sc.close();
	        }

	        for (int i = 0; i < tmesh.number_faces; i++) {
	            line = br.readLine();
	            while ((line = br.readLine()) != null && !line.trim().startsWith("f")) {
	                if (line.trim().isEmpty()) {
	                    continue;  // Skip empty lines
	                }
	                sc = new Scanner(line);
	                // Skip "f"
	                sc.next();
	                
	                int fi = sc.nextInt();
	                int fj = sc.nextInt();
	                int fk = sc.nextInt();
	                
	                tmesh.setFace(i, fi - 1, fj - 1, fk - 1); // Subtract 1 to convert to zero-based indexing
	                sc.close();
	            }
	        }

	        br.close();

//	        System.out.println("Read mesh: " + filename);
//	        System.out.println("made of " + tmesh.number_vertices + " vertices and " + tmesh.number_faces + " faces.");

	        return true;
	    } catch (IOException e) {
	        System.out.println("An exception occurred while reading " + filename);
	        return false;
	    }
	}
}
