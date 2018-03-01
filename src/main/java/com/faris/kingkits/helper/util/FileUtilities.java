package com.faris.kingkits.helper.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

public class FileUtilities {

	public static void copyFolder(File sourceDir, File destinationDir, String... ignoredFiles) throws IOException {
		if (sourceDir != null && destinationDir != null && sourceDir.exists()) {
			copyDirectory(sourceDir.toPath(), destinationDir.toPath());
			if (ignoredFiles != null) {
				for (String ignoredFilePath : ignoredFiles) {
					File ignoredFile = new File(destinationDir, ignoredFilePath);
					if (ignoredFile.exists()) delete(ignoredFile);
				}
			}
		}
	}

	private static void copyDirectory(final Path source, final Path target) throws IOException {
		Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new FileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes sourceBasic) throws IOException {
				Path targetDir = Files.createDirectories(target.resolve(source.relativize(dir)));
				AclFileAttributeView acl = Files.getFileAttributeView(dir, AclFileAttributeView.class);
				if (acl != null) Files.getFileAttributeView(targetDir, AclFileAttributeView.class).setAcl(acl.getAcl());
				DosFileAttributeView dosAttrs = Files.getFileAttributeView(dir, DosFileAttributeView.class);
				if (dosAttrs != null) {
					DosFileAttributes sourceDosAttrs = dosAttrs.readAttributes();
					DosFileAttributeView targetDosAttrs = Files.getFileAttributeView(targetDir, DosFileAttributeView.class);
					targetDosAttrs.setArchive(sourceDosAttrs.isArchive());
					targetDosAttrs.setHidden(sourceDosAttrs.isHidden());
					targetDosAttrs.setReadOnly(sourceDosAttrs.isReadOnly());
					targetDosAttrs.setSystem(sourceDosAttrs.isSystem());
				}
				FileOwnerAttributeView ownerAttrs = Files.getFileAttributeView(dir, FileOwnerAttributeView.class);
				if (ownerAttrs != null) {
					FileOwnerAttributeView targetOwner = Files.getFileAttributeView(targetDir, FileOwnerAttributeView.class);
					targetOwner.setOwner(ownerAttrs.getOwner());
				}
				PosixFileAttributeView posixAttrs = Files.getFileAttributeView(dir, PosixFileAttributeView.class);
				if (posixAttrs != null) {
					PosixFileAttributes sourcePosix = posixAttrs.readAttributes();
					PosixFileAttributeView targetPosix = Files.getFileAttributeView(targetDir, PosixFileAttributeView.class);
					targetPosix.setPermissions(sourcePosix.permissions());
					targetPosix.setGroup(sourcePosix.group());
				}
				UserDefinedFileAttributeView userAttrs = Files.getFileAttributeView(dir, UserDefinedFileAttributeView.class);
				if (userAttrs != null) {
					UserDefinedFileAttributeView targetUser = Files.getFileAttributeView(targetDir, UserDefinedFileAttributeView.class);
					for (String key : userAttrs.list()) {
						ByteBuffer buffer = ByteBuffer.allocate(userAttrs.size(key));
						userAttrs.read(key, buffer);
						buffer.flip();
						targetUser.write(key, buffer);
					}
				}
				BasicFileAttributeView targetBasic = Files.getFileAttributeView(targetDir, BasicFileAttributeView.class);
				targetBasic.setTimes(sourceBasic.lastModifiedTime(), sourceBasic.lastAccessTime(), sourceBasic.creationTime());
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.copy(file, target.resolve(source.relativize(file)), StandardCopyOption.COPY_ATTRIBUTES);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException e) throws IOException {
				throw e;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException {
				if (e != null) throw e;
				return FileVisitResult.CONTINUE;
			}
		});
	}

	public static boolean createDirectory(File folder) {
		if (folder != null) {
			try {
				return folder.exists() || folder.mkdirs();
			} catch (Exception ignored) {
			}
		}
		return false;
	}

	public static void delete(File source) {
		delete(source, false);
	}

	private static void delete(File source, boolean isLoop) {
		try {
			if (!source.exists()) return;
			if (source.isDirectory()) {
				File[] dirFiles = source.listFiles();
				if (dirFiles != null) {
					for (File subFile : dirFiles) delete(subFile, true);
				}
			}
			source.delete();
		} catch (Exception ex) {
			if (!isLoop) ex.printStackTrace();
		}
	}

	public static void deleteInside(File source) {
		deleteInside(source, false);
	}

	private static void deleteInside(File source, boolean isLoop) {
		try {
			if (source.isDirectory()) {
				File[] dirFiles = source.listFiles();
				if (dirFiles != null) {
					for (File subFile : dirFiles) delete(subFile, true);
				}
			}
			if (isLoop) source.delete();
		} catch (Exception ex) {
			if (!isLoop) ex.printStackTrace();
		}
	}

	public static File[] getFiles(File folder) {
		if (folder != null && folder.exists()) {
			File[] files = folder.listFiles();
			if (files != null) return files;
		}
		return new File[0];
	}

	public static List<String> readFile(File file) throws Exception {
		List<String> configLines = new ArrayList<>();
		if (file == null || !file.exists()) return configLines;

		BufferedReader configReader = null;
		try {
			configReader = new BufferedReader(new FileReader(file));
			String configLine;
			while ((configLine = configReader.readLine()) != null) configLines.add(configLine);
		} finally {
			Utilities.silentlyClose(configReader);
		}
		return configLines;
	}

}
